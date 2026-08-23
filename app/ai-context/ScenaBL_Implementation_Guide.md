# ScenaBL — Implementation Guide

Ovaj dokument je tehnički plan implementacije za ScenaBL, zasnovan na `ScenaBL_Improved_SRS.md` i na obrascima verifikovanim u kodu referentne aplikacije "Caster" (Fishing App), u istom repozitorijumu. Cilj je da se iskoriste dokazani, radni dijelovi Fishing App-a (Firebase integracija, Retrofit stil, coroutine/`Flow` upotreba), uz nadogradnju arhitekture na standard koji zahtijeva ovaj kurs (MVVM, Repository sloj, strukturisano stanje) — pošto Fishing App tu arhitekturu **nema** (vidi poglavlje 5 za detaljnu mapu razlika).

> **Izmjena u odnosu na prvobitni plan — čuvanje slika:** Firebase Storage zahtijeva Blaze (plaćeni) plan, što nije realna opcija za studentski projekat bez budžeta. Odluka je promijenjena: slike (profilna slika, slika naslova) se čuvaju preko **ImgBB** (besplatan servis za hosting slika, dostupan preko REST API-ja), na isti način na koji to već radi referentna Fishing App aplikacija. Firebase se i dalje koristi za Authentication i Firestore. Ova izmjena je ograničena na skladištenje slika — ne utiče na ostatak arhitekture (MVVM, Repository sloj, Firestore kao primarna baza).

---

## 1. Tehnološki stek

| Tehnologija | Upotreba | Obrazloženje |
|---|---|---|
| **Android Studio + Kotlin** | Platforma i jezik | Zahtjev projekta; isto kao Fishing App. |
| **Jetpack Compose** | UI sloj | Moderan, deklarativan pristup UI-ju; iako Fishing App koristi XML/View sistem, Compose je danas standardni izbor za nove Android projekte i bolje se uklapa sa `StateFlow`-om iz ViewModel-a. |
| **Cloud Firestore** | Baza podataka | Isti servis koji Fishing App već koristi i dokazano radi (`FirebasePinManager`, kolekcije `map_pins`/`feed_posts`) — realtime sinhronizacija bez potrebe za sopstvenim serverom. |
| **Firebase Authentication** | Registracija/prijava | Fishing App već koristi Firebase Auth (doduše anonimni). ScenaBL prelazi na email/lozinka prijavu, koristeći isti SDK. |
| **ImgBB (REST API)** | Čuvanje slika | Firebase Storage zahtijeva plaćeni (Blaze) plan, što nije opcija za studentski projekat. ImgBB je besplatan servis za hosting slika koji Fishing App već dokazano koristi (`imgbb.com` upload preko REST poziva); ScenaBL preuzima isti obrazac umjesto Firebase Storage-a. |
| **Kotlin Coroutines + Flow** | Asinhrono programiranje | Fishing App već ima jedan dobar primjer (`FirebasePinManager.getPinsFlow()` preko `callbackFlow`); ScenaBL ovaj obrazac generalizuje na sve repozitorijume, umjesto da (kao ostatak Fishing App-a) koristi callback-e (`onSuccess`/`onError`). |
| **Navigation Compose** | Navigacija | Zamjenjuje Fishing App-ov obrazac više Activity-ja povezanih Intent-ima i dupliranom `BottomNavigationView` logikom u svakoj Activity-ju — jedan `NavHost` u jednoj Activity-ju je standard za Compose aplikacije i eliminiše duplikat kod. |
| **Coil** | Učitavanje slika u UI | Compose-native biblioteka za slike (analogno Glide-u koji se pominje u ScenaBL.md v1.0, ali je Coil prirodniji izbor uz Compose). |
| **Manual DI (AppContainer)** | Injekcija zavisnosti | Fishing App nema DI uopšte (ručno instanciranje po Activity-ju). Uvodi se lagani ručni DI kontejner — dovoljno da se izbjegne dupliranje instanciranja, bez uvođenja Hilt-a/Dagger-a čija bi složenost bila neopravdana za obim ovog projekta. |
| **kotlinx-coroutines-play-services / Firestore KTX** | Idiomatski Kotlin API nad Firebase SDK-om | Isto kao Fishing App (`firebase-firestore-ktx`, `firebase-auth-ktx`). |

---

## 2. Arhitektura i struktura paketa

Fishing App nema nikakvu slojevitu strukturu — svih 17 Kotlin fajlova je u jednom paketu (`com.example.caster`), bez `ui/`, `viewmodel/`, `repository/`, `data/` potpaketa, i sva logika je direktno u tri `AppCompatActivity` klase. ScenaBL uvodi standardnu MVVM slojevitu strukturu:

```text
com.example.scenabl/
  data/
    model/          // Korisnik, Naslov, Izvodjenje, Rezervacija, Recenzija, KorisnickaLista (data class-e)
    remote/          // Firebase wrapper klase (Firestore/Auth pristup) + ImgBB REST klijent — DAO ekvivalent
    repository/      // AuthRepository, UserRepository, TitleRepository, PerformanceRepository,
                      // ReservationRepository, ReviewRepository, UserListRepository
  ui/
    screens/          // Compose ekrani: Auth, Home, TitleDetails, Reservation, MyLists,
                      // MyReservations, Profile, OrganizerDashboard, OrganizerForms
    components/       // Djeljene Composable komponente (kartica naslova, zvjezdice, filter-chip...)
    navigation/        // NavHost + sealed class Screen (rute)
    theme/              // Boje, tipografija, oblici (Material 3)
  viewmodel/           // Po jedan ViewModel po ekranu, izlaže StateFlow<UiState>
  di/                    // AppContainer — ručna injekcija singletona
```

**Odgovornosti slojeva:**

- **`ui/`** — isključivo prikaz stanja i prosljeđivanje korisničkih akcija ViewModel-u; ne sadrži poslovnu logiku niti direktne pozive Firebase SDK-a (za razliku od Fishing App-a, gdje Activity direktno poziva `FirebasePinManager`/`FirebaseFirestore`).
- **`viewmodel/`** — drži `MutableStateFlow<UiState>` po ekranu, poziva repository funkcije unutar `viewModelScope`, validira formu prije slanja.
- **`repository/`** — jedina tačka koja poziva `remote/` klase; vraća `Flow`/`suspend` rezultate, po potrebi mapira Firebase greške u čitljive poruke.
- **`remote/`** — tanak omotač oko Firestore/Auth poziva i ImgBB REST poziva za upload slika (analogno `FirebasePinManager` iz Fishing App-a, ali dosljedno `suspend`/`Flow`, ne mješavina callback-a i Flow-a).
- **`di/AppContainer`** — kreira i drži singltone repozitorijuma/remote klasa; svaka Activity/Application ih dobija iz jednog mjesta, umjesto ručnog `= FirebasePinManager()` na više mjesta (kako to radi Fishing App).

---

## 3. Dizajn baze podataka (Cloud Firestore)

Pošto ne postoji relaciona baza, DAO sloj se ovdje realizuje kao **remote data source klase** koje omotavaju Firestore pozive. Ispod je struktura kolekcija (identična onoj iz SRS-a, poglavlje 6.1) uz CRUD obrazac za svaki entitet.

### 3.1 Entiteti

| Kolekcija | Ključna polja | Reference |
|---|---|---|
| `users/{uid}` | ime, prezime, email, role, favoriteGenres[], profileImageUrl | id = Firebase Auth UID |
| `institutions/{id}` | naziv, opis, ownerUid | ownerUid → users |
| `titles/{id}` | naziv, opis, reziser, trajanje, zanr, tip, slikaUrl, institutionId | institutionId → institutions |
| `performances/{id}` | titleId, institutionId, datumVrijeme, sala, kapacitet, rezervisano, cijena | titleId → titles, institutionId → institutions |
| `reservations/{id}` | userId, performanceId, brojKarata, status, datumKreiranja | userId → users, performanceId → performances |
| `reviews/{id}` | userId, titleId, ocjena, komentar, datum | userId → users, titleId → titles |
| `userLists/{id}` | userId, titleId, tipListe | userId → users, titleId → titles |

### 3.2 Remote data source (DAO ekvivalent) — obrazac

Po uzoru na `FirebasePinManager.getPinsFlow()` (jedini već-dobar obrazac u Fishing App-u), ali primijenjen dosljedno svuda:

```kotlin
class TitleRemoteDataSource(private val firestore: FirebaseFirestore) {

    fun observeTitles(): Flow<List<Naslov>> = callbackFlow {
        val listener = firestore.collection("titles")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObjects(Naslov::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun addTitle(title: Naslov): String =
        firestore.collection("titles").add(title).await().id
}
```

Ovo direktno nasljeđuje `callbackFlow` + `awaitClose` obrazac iz `FirebasePinManager.kt`, ali koristi `suspend fun ... .await()` (kotlinx-coroutines-play-services) umjesto `addOnSuccessListener`/`addOnFailureListener` callback parova koje Fishing App koristi za pisanje — čime se eliminiše "callback pakao" i omogućava korišćenje `try/catch` u ViewModel-u.

### 3.3 Repository sloj — obrazac

```kotlin
class TitleRepository(private val remote: TitleRemoteDataSource) {
    fun observeTitles(): Flow<List<Naslov>> = remote.observeTitles()

    suspend fun createTitle(title: Naslov): Result<String> = runCatching {
        remote.addTitle(title)
    }
}
```

Repository je jedina klasa koju ViewModel poznaje — ViewModel nikada ne uvozi `FirebaseFirestore` direktno (za razliku od Fishing App-a, gdje `MainActivity`, `MapManager` i `FeedActivity` sve rade direktno sa Firestore/manager klasama).

### 3.4 CRUD po entitetu (sažetak)

| Entitet | Create | Read | Update | Delete |
|---|---|---|---|---|
| **Naslov** | Ustanova kreira kroz `OrganizerTitleFormScreen` → `TitleRepository.createTitle` | `HomeScreen`/`TitleDetailsScreen` preko `observeTitles()`/`observeTitle(id)` (realtime) | Ustanova uređuje kroz istu formu → `updateTitle` | Van v1.0 opsega (arhiviranje umjesto brisanja, da se ne pokidaju postojeće rezervacije/recenzije) |
| **Izvođenje** | Ustanova kroz `OrganizerPerformanceFormScreen` | `TitleDetailsScreen` (lista izvođenja za naslov) | Otkazivanje mijenja status polje, ne briše dokument | — |
| **Rezervacija** | `ReservationScreen` → Firestore transakcija (upis + inkrement `rezervisano`) | `MyReservationsScreen`, `OrganizerDashboardScreen` | Otkazivanje → `status = cancelled` + dekrement | Nema fizičkog brisanja (istorija se čuva) |
| **Recenzija** | `TitleDetailsScreen` → `ReviewRepository.upsertReview` (insert ili update ako već postoji za taj par userId/titleId) | Lista na `TitleDetailsScreen` | Isti upsert poziv | Korisnik može obrisati sopstvenu recenziju |
| **Lična lista** | Dodavanje dugmetom na `TitleDetailsScreen` | `MyListsScreen` (tabovi) | Prebacivanje iz "želim gledati" u "odgledano" | Uklanjanje iz liste |

Rezervacija je jedini entitet kojem je potrebna **Firestore transakcija** (`firestore.runTransaction { ... }`) umjesto prostog upisa, jer mora atomično provjeriti i uvećati `rezervisano` na `performances/{id}` — ovo je nova, dodatna funkcionalnost u odnosu na Fishing App, koji nema nijednu operaciju koja zahtijeva atomičnost (sve njegove Firestore operacije su nezavisni jednostavni upisi).

---

## 4. Arhitektura ekrana i navigacije

| Ekran | ViewModel | Repository/izvor podataka koje koristi |
|---|---|---|
| AuthScreen | AuthViewModel | AuthRepository, UserRepository |
| HomeScreen | HomeViewModel | PerformanceRepository, TitleRepository |
| TitleDetailsScreen | TitleDetailsViewModel | TitleRepository, PerformanceRepository, ReviewRepository, UserListRepository |
| ReservationScreen | ReservationViewModel | ReservationRepository, PerformanceRepository |
| MyListsScreen | MyListsViewModel | UserListRepository, TitleRepository |
| MyReservationsScreen | MyReservationsViewModel | ReservationRepository |
| ProfileScreen | ProfileViewModel | UserRepository, ImgBB (preko UserRepository) |
| OrganizerDashboardScreen | OrganizerViewModel | TitleRepository, PerformanceRepository, ReservationRepository |
| OrganizerTitleFormScreen / OrganizerPerformanceFormScreen | OrganizerFormViewModel | TitleRepository, PerformanceRepository |

Navigacija: jedan `MainActivity` sa `NavHost`-om i sealed `Screen` klasom za rute (zamjenjuje Fishing App-ove tri odvojene `Activity` povezane `Intent`-ima). Donja navigaciona traka (Home / Moje liste / Moje rezervacije / Profil za gledaoce; Repertoar / Rezervacije za ustanove) implementirana je jednom, u zajedničkom `Scaffold`-u oko `NavHost`-a — umjesto dupliranog `BottomNavigationView` koda u svakoj Activity-ju kao u Fishing App-u (`MainActivity.kt`, `FeedActivity.kt`, `InventoryActivity.kt` svaka ponavljaju istu logiku).

Gost (bez prijave) ulazi direktno na `HomeScreen` u read-only režimu; pokušaj rezervacije/recenzije/liste vodi ga na `AuthScreen`.

---


## 5. Redoslijed implementacije

1. Kreiranje Android projekta (Empty Compose Activity), podešavanje `applicationId`, minSdk 26.
2. Podešavanje Firebase projekta (Firestore, Authentication — email/lozinka provider) i dodavanje `google-services.json` (isti korak kao u Fishing App-u). Firebase Storage se ne koristi (zahtijeva plaćeni Blaze plan); umjesto toga koristi se ImgBB nalog/API ključ za upload slika.
3. Dodavanje zavisnosti: Compose BOM, Navigation Compose, Firebase BOM (firestore-ktx, auth-ktx — bez storage-ktx), Coil, kotlinx-coroutines-play-services, Retrofit/OkHttp (za ImgBB REST pozive).
4. Definisanje data klasa u `data/model/` (Korisnik, Naslov, Izvodjenje, Rezervacija, Recenzija, KorisnickaLista).
5. Implementacija `data/remote/` klasa (Firestore/Auth omotači + ImgBB REST klijent) — po uzoru na `FirebasePinManager`.
6. Implementacija `data/repository/` klasa iznad remote sloja.
7. Implementacija `di/AppContainer`-a koji kreira i drži repository singltone.
8. Implementacija ViewModel-a po ekranu, sa `StateFlow<UiState>`.
9. Implementacija `navigation/` (sealed `Screen`, `NavHost`) i zajedničkog `Scaffold`-a sa donjom navigacijom.
10. Implementacija Compose ekrana, počevši od `AuthScreen` i `HomeScreen`, zatim `TitleDetailsScreen`, pa redom ostalih.
11. Implementacija CRUD tokova: prvo pregled/pretraga (najjednostavnije, samo čitanje), zatim rezervacija (zahtijeva transakciju), zatim recenzije i lične liste, na kraju organizator ekrani.
12. Validacija formi i obrada grešaka (lozinka, obavezna polja, kapacitet, rok za otkazivanje) u ViewModel sloju.
13. Podešavanje Firestore Security Rules (uloga, vlasništvo dokumenta) prije puštanja u produkciju.
14. Testiranje: jedinični testovi za ViewModel (sa fake/mock repository implementacijama) i repository logiku (npr. provjera kapaciteta); ručno testiranje UI toka.
15. Finalno čišćenje: uklanjanje debug logova, provjera da nijedan API ključ nije hardkodovan u izvornom kodu (Fishing App ima ovaj propust sa OpenWeatherMap ključem — izbjeći ga korišćenjem `local.properties`/`BuildConfig`, kao što Fishing App već radi za imgbb ključ; ScenaBL koristi isti obrazac i za svoj ImgBB ključ).

---

## 6. Profesorova lista provjere (checklist)

| Zahtjev iz `ProffesorSuggestion.md` | Kako je ispunjen |
|---|---|
| Standardna, dosljedna struktura (Uvod/Opšti opis/Funkcije/Spoljni interfejsi/NFR/Dodaci) | SRS poglavlja 1–6 prate tačno ovaj redoslijed. |
| Dosljednost jezika (bez mješanja unutar rečenice) | Cijeli SRS je na srpskom (latinica), sa dosljedno korišćenim engleskim tehničkim terminima (Firebase, Firestore, API, backend) — isti stil kao u ScenaBL.md v1.0. |
| Specifični, mjerljivi zahtjevi umjesto nejasnih fraza | Svi NFR u SRS poglavlju 5 su kvantifikovani (npr. NFR-PERF-001: "< 1,5 s"); funkcionalni zahtjevi u poglavlju 3 imaju REQ-ID i konkretne brojeve/uslove. |
| Detaljno opisani filteri (koji, kako, kakav rezultat) | SRS REQ-BROW-003 eksplicitno navodi dostupne filtere, UI kontrole i ponašanje rezultata. |
| Performanse — kvantitativne metrike | SRS NFR-PERF-001 do 003. |
| Bezbjednost — pravila lozinke, sesije, enkripcija | SRS NFR-SEC-001 do 004 (uz eksplicitno obrazloženo odstupanje kod sesija, pošto Firebase Auth ne radi na server-timeout modelu). |
| Upotrebljivost — referenca na UI/UX nacrt | SRS NFR-USAB-003 (preporuka izrade wireframe-a prije implementacije, vidi napomenu ispod). |
| Pouzdanost/dostupnost — ponašanje pri gubitku mreže | SRS NFR-REL-001 (Firestore offline keš + korisnička poruka). |
| API-jevi — konkretni endpoint-i i tehnologija | SRS poglavlje 4.4 — pošto nema REST servera, tabela eksplicitno mapira svaku planiranu operaciju na konkretan Firestore/Auth SDK poziv, umjesto da samo kaže "koristi se Firebase". |
| Format podataka — eksplicitno JSON | SRS poglavlje 4.4, sa primjerom dokumenta. |
| Usluge trećih strana imenovane sa funkcijom | SRS poglavlje 4.3 (Firebase Authentication, Firestore, Security Rules, ImgBB — svaka sa opisanom ulogom). |
| Rečnik ključnih pojmova | SRS poglavlje 1.3 (tabela pojmova). |
| Jasno definisana ograničenja (tehnička i poslovna) | SRS poglavlje 2.4. |
| Realan opseg, jasno šta je uključeno a šta ne | SRS poglavlje 2.5 (MVP — eksplicitna lista van-opsega stavki sa obrazloženjem). |
| Use Case dijagram | SRS poglavlje 6.2 (Mermaid dijagram aktera i funkcija). |
| Arhitekturni dijagram (app/backend/baza) | SRS poglavlje 2.1 (Mermaid dijagram). |
| Figma/UI nacrti za složene tokove | Nije izrađen u okviru ovog dokumenta (van opsega dvije tražene SRS/Implementation Guide markdown datoteke); SRS NFR-USAB-003 eksplicitno preporučuje izradu wireframe-a kao sljedeći korak prije početka implementacije ekrana — ovo je jedina profesorova preporuka koja ovdje nije potpuno ispunjena, i ta neizvjesnost je ovdje eksplicitno naznačena, u skladu sa pravilom da se nejasnoće ne prećutkuju. |
