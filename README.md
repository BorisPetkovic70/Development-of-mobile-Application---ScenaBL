# ScenaBL

**ScenaBL** je mobilna aplikacija za Android koja korisnicima omogućava pregled aktuelnog repertoara pozorišnih predstava i bioskopskih projekcija, rezervaciju karata, vođenje ličnih listi i ocjenjivanje odgledanog sadržaja. Ustanovama (pozorištima i bioskopima) aplikacija omogućava kreiranje i upravljanje sopstvenim repertoarom.

Aplikacija je razvijena kao završni projekat na predmetu **Razvoj smartfon-aplikacija**.

---

## Sadržaj

- [Snimci ekrana](#snimci-ekrana)
- [Ključne funkcionalnosti](#ključne-funkcionalnosti)
- [Tehnologije](#tehnologije)
- [Arhitektura](#arhitektura)
- [Pokretanje projekta](#pokretanje-projekta)
- [Autor](#autor)

---

## Snimci ekrana

*Ova sekcija će biti dopunjena snimcima ekrana aplikacije.*

| Repertoar | Detalji naslova | Rezervacija |
|---|---|---|
| _(slika)_ | _(slika)_ | _(slika)_ |

| Moje liste | Recenzije | Organizator |
|---|---|---|
| _(slika)_ | _(slika)_ | _(slika)_ |

---

## Ključne funkcionalnosti

Funkcionalnosti aplikacije zavise od uloge prijavljenog korisnika.

### Gost (bez prijave)

- Pregled aktuelnog repertoara predstava i filmova.
- Pretraga i filtriranje repertoara (naziv, žanr, datum, tip sadržaja).
- Pregled detalja naslova, predstojećih izvođenja i postojećih recenzija.
- Pristup rezervaciji, recenzijama i ličnim listama vodi na ekran za prijavu.

### Registrovani gledalac

- Sve funkcionalnosti dostupne Gostu.
- Rezervacija karata za odabrano izvođenje, uz atomičnu provjeru dostupnih mjesta.
- Otkazivanje sopstvenih rezervacija (najkasnije 2 sata prije početka izvođenja).
- Vođenje ličnih listi — "Želim gledati" i "Odgledano".
- Ocjenjivanje (1–5 zvjezdica) i pisanje recenzija za odgledane naslove.
- Uređivanje profila (ime, profilna slika, omiljeni žanrovi).

### Ustanova (Organizator)

- Kreiranje i uređivanje sopstvenih naslova (predstava/filmova).
- Zakazivanje i otkazivanje izvođenja (datum, vrijeme, sala, kapacitet, cijena).
- Pregled sopstvenog repertoara sa brojem rezervisanih/preostalih mjesta po terminu, ažurirano u realnom vremenu.
- Podešavanje osnovnih podataka o ustanovi pri prvom korišćenju.

---

## Tehnologije

- **Kotlin** — programski jezik aplikacije.
- **Jetpack Compose** — deklarativni UI toolkit korišten za izgradnju svih ekrana aplikacije, bez XML layout fajlova.
- **Firebase Authentication** — registracija i prijava korisnika i ustanova (e-mail/lozinka).
- **Cloud Firestore** — NoSQL baza podataka za sve poslovne podatke (naslovi, izvođenja, rezervacije, recenzije, lične liste), sa realtime sinhronizacijom preko listenera.
- **Firestore Security Rules** — autorizacija pristupa podacima na nivou dokumenta/kolekcije.
- **ImgBB** — spoljni REST servis za hosting slika (profilne slike i slike naslova); Firestore dokument čuva samo vraćeni URL slike.
- **Retrofit / OkHttp** — komunikacija sa ImgBB REST API-jem.
- **Coil** — asinhrono učitavanje i prikaz slika unutar Compose UI-ja.
- **Kotlin Coroutines / Flow** — asinhrone operacije i realtime tokovi podataka.

---

## Arhitektura

Aplikacija je strukturirana po strogoj **MVVM (Model–View–ViewModel)** arhitekturi, sa jasno odvojenim slojevima:

- **UI (View)** — Compose ekrani koji prikazuju stanje i prosljeđuju korisničke akcije dalje, bez sopstvene poslovne logike.
- **ViewModel** — po jedan ViewModel po ekranu, izlaže stanje ekrana kroz `StateFlow<UiState>` i sadrži logiku specifičnu za taj ekran.
- **Repository** — javni sloj prema kojem ViewModel-i pristupaju podacima; ne zna za Firebase/ImgBB detalje.
- **Remote Data Source** — jedini sloj koji direktno komunicira sa Firebase SDK-om i ImgBB REST API-jem.

Tok zavisnosti ide isključivo u jednom smjeru:

```
UI → ViewModel → Repository → Remote Data Source → Firebase / ImgBB
```

UI i ViewModel slojevi nikada ne pristupaju Firebase SDK-u direktno — svaki pristup podacima ide isključivo preko Repository sloja, čime se omogućava buduća zamjena backend-a bez izmjene UI koda. Zavisnosti između slojeva ubrizgavaju se preko ručnog DI kontejnera (`AppContainer`), bez dodatnih DI biblioteka.

---

## Pokretanje projekta

Za pokretanje projekta na lokalnoj mašini potrebno je:

1. **Klonirati repozitorijum**

   ```
   git clone <URL-repozitorijuma>
   ```

2. **Podesiti Firebase**

   Aplikacija zahtijeva sopstveni Firebase projekat sa uključenim **Authentication** (email/lozinka) i **Cloud Firestore** servisima.

   - Kreirati novi projekat na [Firebase Console](https://console.firebase.google.com/).
   - Registrovati Android aplikaciju sa `applicationId`: `com.example.scenabl`.
   - Preuzeti generisani `google-services.json` fajl i smjestiti ga u `app/` direktorijum projekta.
   - Postaviti pravila pristupa iz `firestore.rules` (u korijenu repozitorijuma) u Firebase Console → Firestore Database → Rules.

3. **Podesiti ImgBB API ključ**

   Slike (profilne slike i slike naslova) čuvaju se preko [ImgBB](https://api.imgbb.com/) besplatnog REST servisa.

   - Kreirati besplatan API ključ na ImgBB.
   - U korijenu projekta, u fajlu `local.properties`, dodati liniju:

     ```
     IMGBB_API_KEY=vaš_api_ključ
     ```

4. **Otvoriti i pokrenuti projekat**

   - Otvoriti projekat u **Android Studio** (preporučena najnovija stabilna verzija).
   - Sačekati da Gradle sinhronizuje projekat i preuzme sve zavisnosti.
   - Pokrenuti aplikaciju na emulatoru ili fizičkom uređaju sa Android 8.0 (API 26) ili novijim.

---

## Autor

**Boris Petković**
Predmet: Razvoj smartfon-aplikacija
