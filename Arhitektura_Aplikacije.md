# Arhitektura aplikacije ScenaBL

> Konceptualni pregled kodne baze, namijenjen pripremi za odbranu projekta.
> Ne opisuje svaku liniju koda, već način razmišljanja iza strukture projekta.

---

## 1. Glavne tehnologije i koncepti

### Jetpack Compose

Jetpack Compose je moderni deklarativni UI toolkit za Android. Umjesto da se
ekran gradi kroz XML layout fajlove i imperativno "ručno" ažuriranje pogleda
(`findViewById`, `setText`, itd.), u Compose-u se UI piše kao obične Kotlin
funkcije označene sa `@Composable` (npr. `HomeScreen`, `TitleDetailsScreen`).

Ključna ideja: **UI je funkcija stanja** — `UI = f(state)`. Ekran se ne
"osvježava ručno"; umjesto toga, on čita stanje (npr. `StateFlow` iz
ViewModel-a) i Compose sam ponovo iscrta (rekompozicija) samo one dijelove
ekrana čiji se ulazni podaci promijene. To je razlog zašto se u kodu stalno
vidi obrazac:

```kotlin
val state by viewModel.uiState.collectAsState()
```

Ovim se Compose "pretplaćuje" na promjene stanja — čim ViewModel emituje novo
stanje, ekran se automatski ponovo iscrtava.

### MVVM (Model – View – ViewModel) u ovom projektu

MVVM razdvaja odgovornosti u tri sloja, i u ScenaBL-u je to strogo ispoštovano:

- **View (Model sloja prikaza)** — Compose ekrani u `ui/screens`. Njihov jedini
  posao je da prikažu trenutno stanje i proslijede korisničke akcije (klikove)
  dalje. Ekrani **ne sadrže poslovnu logiku** — ne računaju da li ima
  slobodnih mjesta, ne pozivaju Firebase direktno, samo pozivaju funkcije na
  ViewModel-u (npr. `viewModel.confirmReservation()`).
- **ViewModel** — po jedan ViewModel po ekranu (`ReservationViewModel`,
  `HomeViewModel`, itd.), u paketu `viewmodel/`. Drži stanje ekrana u obliku
  `StateFlow<UiState>`, sadrži poslovnu/prezentacionu logiku (validacija,
  kombinovanje više izvora podataka, upravljanje "loading/error" stanjima) i
  poziva Repository sloj. ViewModel **preživljava rotaciju ekrana** i ne zna
  ništa o Compose-u — ne uvozi nijedan `androidx.compose.*` UI element.
- **Model** — u ovom projektu to su Kotlin data klase u `data/model/`
  (`Naslov`, `Izvodjenje`, `Rezervacija`...) koje predstavljaju strukturu
  podataka, plus sloj koji te podatke pribavlja (Repository + DataSource,
  objašnjeno niže).

Tok zavisnosti ide **samo u jednom smjeru**: `UI → ViewModel → Repository →
DataSource → Firebase`. Sloj gore nikad ne "preskače" sloj ispod, i nijedan
sloj ne zna za slojeve iznad sebe (Repository ne zna da postoji ViewModel).

### Firebase kao zamjena za tradicionalni backend

Umjesto da se piše sopstveni server (npr. Spring Boot + PostgreSQL + REST API
+ hosting), ScenaBL koristi Firebase kao "backend-as-a-service":

- **Cloud Firestore** — NoSQL baza podataka orijentisana na dokumente
  (kolekcije poput `titles`, `performances`, `reservations`). Zamjenjuje i
  bazu podataka *i* REST API — Android aplikacija razgovara direktno sa
  Firestore-om preko zvaničnog SDK-a, bez posrednog servera. Firestore nudi
  **realtime listenere** (`addSnapshotListener`) — kada se podatak promijeni
  na serveru, svi klijenti koji "slušaju" tu kolekciju automatski dobijaju
  novu verziju podataka, bez ručnog "refresh-a".
- **Firebase Authentication** — zamjenjuje sopstvenu logiku za registraciju,
  hashovanje lozinki, izdavanje i verifikaciju sesijskih tokena. Aplikacija
  samo poziva `signInWithEmailAndPassword()` / `createUserWithEmailAndPassword()`,
  a Firebase upravlja sigurnošću sesije.
- **Firestore Security Rules** — zamjenjuju sloj autorizacije koji bi inače
  živio na serveru (npr. `if (user.role != "organizer") throw 403`). Pravila
  se pišu deklarativno na Firebase strani i kažu ko smije čitati/pisati koji
  dokument — npr. da samo vlasnik ustanove (`ownerUid`) smije mijenjati svoje
  naslove.
- **ImgBB** (spoljni REST servis, ne dio Firebase-a) — koristi se samo za
  slike, jer je jednostavan besplatan servis za hosting slika. Slika se
  uploaduje preko Retrofit-a, a Firestore čuva samo vraćeni URL kao tekstualno
  polje u dokumentu.

Efekat: aplikacija nema sopstveni server, ali i dalje ima bazu, autentifikaciju
i autorizaciju — samo su prebačeni na Google-ovu infrastrukturu.

---

## 2. Struktura foldera (Package Structure)

```
com.example.scenabl/
│
├── MainActivity.kt              Ulazna Activity — samo postavlja Compose sadržaj
├── ScenaBLApplication.kt        Application klasa — kreira AppContainer (DI)
│
├── di/
│   └── AppContainer.kt          Ručni DI kontejner — pravi i drži sve
│                                 repository/datasource singletone
│
├── data/
│   ├── model/                   Čiste Kotlin data klase (Naslov, Izvodjenje,
│   │                            Rezervacija, Recenzija, Korisnik...)
│   │                            — 1:1 odraz Firestore dokumenata
│   │
│   ├── remote/                  DataSource klase — direktna komunikacija sa
│   │                            Firebase SDK-om i ImgBB Retrofit API-jem
│   │                            (npr. ReservationRemoteDataSource,
│   │                             TitleRemoteDataSource, ImgBbRemoteDataSource)
│   │
│   └── repository/              Repository klase — javni "ugovor" prema
│                                 ViewModel-ima; sakrivaju detalje Firebase-a
│                                 (npr. ReservationRepository, TitleRepository)
│
├── viewmodel/                   Po jedan ViewModel po ekranu, drži
│                                 StateFlow<UiState> i poslovnu logiku ekrana
│                                 (npr. ReservationViewModel, HomeViewModel)
│
└── ui/
    ├── navigation/               ScenaBLApp.kt (NavHost — "kablovi" koji
    │                              spajaju rute, ViewModel-e i ekrane) +
    │                              BottomNavBar, Screen (definicije ruta)
    ├── screens/                  Compose ekrani (jedan fajl po ekranu)
    ├── components/                Djeljene, ponovo iskoristive Compose
    │                              komponente (RatingStars, OfflineBanner...)
    ├── theme/                     Material 3 tema (boje, tipografija)
    └── util/                      Čisti pomoćni fju. (formatiranje datuma...)
```

**Odgovornost svakog sloja, jednom rečenicom:**

| Sloj | Odgovornost |
|---|---|
| `ui/screens` | Prikaz stanja i prosljeđivanje korisničkih akcija dalje. Nema logike. |
| `viewmodel/` | Drži stanje ekrana, sadrži logiku ekrana, poziva repository. Ne zna za Compose. |
| `data/repository` | Javni API prema poslovnim entitetima (npr. "napravi rezervaciju"), skriva Firebase. |
| `data/remote` | Jedino mjesto koje direktno poziva Firebase/Retrofit SDK. |
| `data/model` | Struktura podataka — čist opis "oblika" jednog dokumenta/entiteta. |
| `di/AppContainer` | Ručno kreira i povezuje sve gornje slojeve (bez Hilt/Koin biblioteke). |

---

## 3. Ključne klase i njihova uloga

### `ScenaBLApplication.kt`

Ovo je Android `Application` klasa — kod u njoj se izvršava tačno jednom, prije
bilo koje Activity, kada proces aplikacije krene. Njen jedini posao je da
napravi **jednu** instancu `AppContainer`-a i drži je dok aplikacija živi:

```kotlin
class ScenaBLApplication : Application() {
    lateinit var appContainer: AppContainer
    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer()
    }
}
```

Time se dobija jedan "izvor istine" za sve repository objekte u cijeloj
aplikaciji — svaki ekran dobija *isti* `ReservationRepository`, a ne novu
instancu za svaki ekran.

### `AppContainer` — ručna dependency injection

Pošto projekat ne koristi Hilt/Koin, `AppContainer` je "ručni DI kontejner":
obična klasa sa `lazy` propertijima koja redom pravi Firebase klijente →
DataSource-e → Repository-je, ubrizgavajući svaki niži sloj u viši preko
konstruktora. `ScenaBLApp()` (root Composable u navigaciji) prima
`AppContainer` kao parametar i iz njega vadi repository-je kad god pravi novi
ViewModel preko `viewModelFactory { initializer { ... } }`.

### DataSource vs. Repository — ključna razlika

Ovo je najvažnija arhitekturna distinkcija u projektu i često pitanje na
odbrani:

- **DataSource** (`data/remote/`) — **zna za Firebase**. Sadrži stvarne pozive
  ka `FirebaseFirestore`, `FirebaseAuth` ili Retrofit-u. Radi sa "sirovim"
  Firebase konceptima: `collection()`, `document()`, `runTransaction()`,
  `addSnapshotListener()`. Da sutra treba zamijeniti Firebase nekim drugim
  backend-om, mijenja se **samo ovaj sloj**.
- **Repository** (`data/repository/`) — **ne zna za Firebase** (ne uvozi
  `com.google.firebase.*` osim eventualno tipova modela). To je tanak,
  poslovno-orijentisan omotač oko jednog ili više DataSource-a. Njegov posao
  je da izloži ViewModel-u API u terminima domena ("napravi rezervaciju",
  "posmatraj recenzije za naslov"), i da greške pretvori u standardni Kotlin
  `Result<T>` preko `runCatching { ... }`.

Konkretan primjer (`ReservationRepository`):

```kotlin
class ReservationRepository(private val remote: ReservationRemoteDataSource) {
    suspend fun createReservation(userId: String, performanceId: String, brojKarata: Int): Result<String> =
        runCatching { remote.createReservation(userId, performanceId, brojKarata) }
}
```

Repository ovdje samo delegira poziv i uhvati izuzetak — svu stvarnu logiku
(Firestore transakcija) radi DataSource. Ovo razdvajanje direktno ostvaruje
zahtjev **NFR-MAINT-002** iz SRS-a: "Repository sloj je jedina tačka pristupa
Firebase i ImgBB servisima."

### Opšta uloga ViewModel-a u projektu

Svaki ViewModel u `viewmodel/` prati isti obrazac:

1. Drži privatni, promjenljivi `MutableStateFlow<XyzUiState>` i javno izlaže
   samo read-only `StateFlow` (`val uiState: StateFlow<...> = _uiState.asStateFlow()`).
2. U `init` bloku pokreće coroutine-e (`viewModelScope.launch`) koje se
   pretplaćuju na Firestore realtime tokove iz repository-ja (`Flow`/`collect`)
   i ažuriraju stanje čim stignu novi podaci.
3. Izlaže obične funkcije (`confirmReservation()`, `increment()`,
   `toggleWatchlist()`...) koje UI poziva kao reakciju na klik — te funkcije
   validiraju ulaz, pozivaju repository i ažuriraju `isLoading`/`errorMessage`/
   `isSuccess` polja u stanju.

Drugim riječima: ViewModel je "mozak ekrana" — UI je glup i samo prikazuje
`UiState`, dok ViewModel odlučuje šta se dešava.

---

## 4. Životni ciklus jedne akcije — primjer: klik na "Rezerviši"

Ovo je konkretan primjer za odbranu — praćenje jedne akcije kroz sve slojeve,
od dodira na ekranu do promjene u bazi.

### Korak 1 — Korisnik klikne "Rezerviši" na `TitleDetailsScreen`

Na ekranu detalja naslova, svaki termin izvođenja je prikazan kroz `PerformanceRow`
sa dugmetom:

```kotlin
Button(onClick = onReserveClick, enabled = remaining > 0) {
    Text(if (remaining > 0) "Rezerviši" else "Rasprodato")
}
```

Ovaj `onReserveClick` ne sadrži logiku — on je samo lambda proslijeđena odozgo,
koja (ako je korisnik prijavljen) navigira na ekran za rezervaciju sa ID-om
izvođenja u ruti:

```kotlin
onReserveClick = { if (isLoggedIn) onReserveClick(performance.id) else onLoginRequired() }
```

### Korak 2 — Navigacija kreira `ReservationScreen` + `ReservationViewModel`

U `ScenaBLApp.kt` (`ui/navigation`), `NavHost` prepoznaje rutu `Reservation` i
tu se prvi put **pravi** `ReservationViewModel`, ubrizgavajući mu repository-je
iz `AppContainer`-a:

```kotlin
ReservationViewModel(
    performanceId, uid,
    appContainer.reservationRepository,
    appContainer.performanceRepository,
    appContainer.titleRepository
)
```

Odmah po kreiranju, ViewModel u `init` bloku pokreće realtime listener nad tim
konkretnim izvođenjem (`performanceRepository.observePerformance(performanceId)`),
pa ekran već prikazuje trenutni broj slobodnih mjesta, cijenu i naziv naslova.

### Korak 3 — Korisnik bira broj karata i klikne "Potvrdi rezervaciju"

`+`/`-` dugmad pozivaju `viewModel.increment()` / `decrement()`, koji samo
ažuriraju `ticketCount` u lokalnom stanju (nema mrežnog poziva — to je čisto
UI stanje). Klik na "Potvrdi rezervaciju" poziva:

```kotlin
Button(onClick = viewModel::confirmReservation, ...)
```

### Korak 4 — `ReservationViewModel.confirmReservation()`

```kotlin
fun confirmReservation() = viewModelScope.launch {
    _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
    val ticketCount = _uiState.value.ticketCount
    reservationRepository.createReservation(userId, performanceId, ticketCount).fold(
        onSuccess = { _uiState.update { it.copy(isSubmitting = false, isSuccess = true) } },
        onFailure = { e -> _uiState.update { it.copy(isSubmitting = false, errorMessage = e.message ?: "...") } }
    )
}
```

ViewModel prvo postavlja `isSubmitting = true` (UI odmah prikazuje spinner na
dugmetu), zatim poziva Repository i **čeka** rezultat (`suspend` funkcija), pa
na kraju ažurira stanje na uspjeh ili grešku. UI se automatski ponovo iscrta
zbog `collectAsState()` — nema ručnog pozivanja "refresh" funkcije.

### Korak 5 — `ReservationRepository.createReservation()`

Repository samo delegira poziv DataSource-u i pretvara eventualni izuzetak u
`Result.failure`:

```kotlin
suspend fun createReservation(userId: String, performanceId: String, brojKarata: Int): Result<String> =
    runCatching { remote.createReservation(userId, performanceId, brojKarata) }
```

### Korak 6 — `ReservationRemoteDataSource.createReservation()` — Firestore transakcija

Ovdje se dešava stvarna komunikacija sa Firebase-om, i ovo je srce cijele
funkcionalnosti rezervacije. Koristi se **Firestore transakcija** jer dvije
operacije moraju uspjeti ili propasti *zajedno*, atomično, čak i ako više
korisnika istovremeno rezerviše isto izvođenje:

```kotlin
firestore.runTransaction { transaction ->
    val izvodjenje = transaction.get(performanceRef).toObject(Izvodjenje::class.java)
        ?: error("Izvođenje ne postoji")
    val preostalo = izvodjenje.kapacitet - izvodjenje.rezervisano
    if (preostalo < brojKarata) {
        error("Nema dovoljno slobodnih mjesta (dostupno: $preostalo)")
    }
    val rezervacija = Rezervacija(userId, performanceId, brojKarata, ReservationStatus.ACTIVE)
    transaction.set(reservationRef, rezervacija)
    transaction.update(performanceRef, "rezervisano", izvodjenje.rezervisano + brojKarata)
}.await()
```

Unutar transakcije se dešavaju tri stvari, garantovano atomično:

1. **Čitanje** trenutnog stanja izvođenja (koliko je mjesta već rezervisano).
2. **Provjera kapaciteta** — `kapacitet − rezervisano ≥ traženo` (REQ-RES-002).
   Ako provjera ne prođe, `error(...)` baca izuzetak koji Firestore
   automatski poništava (rollback) — ništa se ne upisuje.
3. Ako provjera prođe: **upis** novog dokumenta u `reservations` kolekciju
   *i* **atomično uvećanje** brojača `rezervisano` na dokumentu izvođenja —
   obje izmjene se commit-uju zajedno ili nijedna.

Ovo sprječava tzv. "race condition" — da dva korisnika istovremeno rezervišu
posljednje slobodno mjesto i oboje uspiju, jer Firestore garantuje da će jedna
od dvije istovremene transakcije nad istim dokumentom biti ponovo pokrenuta
(retry) sa svježim podacima ako se podaci u međuvremenu promijene.

### Korak 7 — Rezultat putuje nazad kroz slojeve

- DataSource vraća ID novog dokumenta (`reservationRef.id`) ili baca izuzetak.
- Repository ga hvata i pakuje u `Result<String>`.
- ViewModel na osnovu `Result`-a ažurira `uiState` (`isSuccess = true` ili
  `errorMessage = "..."`).
- `ReservationScreen` reaguje na `state.isSuccess` preko `LaunchedEffect` i
  poziva `onReserved()`, što u navigaciji vraća korisnika na prethodni ekran
  (`navController.popBackStack()`).
- Zahvaljujući realtime listeneru sa Koraka 2, čak i da korisnik ostane na
  ekranu, broj "Preostalo mjesta" bi se sam ažurirao — jer isti
  `observePerformance()` tok automatski prima novu vrijednost `rezervisano`
  direktno sa Firestore-a, bez ikakvog ručnog osvježavanja.

**Sažetak toka:**

```
Compose UI (klik)
   → ViewModel (validacija, stanje: isSubmitting)
      → Repository (Result<T> omotač, bez Firebase logike)
         → RemoteDataSource (Firestore runTransaction — provjera + upis, atomično)
            → Cloud Firestore (stvarna baza)
         ← Result<String> / Exception
      ← Result<String>
   ← StateFlow update (isSuccess / errorMessage)
UI se automatski rekompajlira (collectAsState) → prikazuje uspjeh ili grešku
```
