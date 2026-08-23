# TEHNIČKI STEK ZA IMPLEMENTACIJU
- Platforma: Android
- Jezik: Kotlin
- UI sistem: [Оvdje unesi: XML ili Jetpack Compose]
- Backend: Firebase (Auth, Firestore, Storage)

---

SOFTWARE REQUIREMENTS SPECIFICATIONS (SRS)
Naziv aplikacije: ScenaBL (Pozorišni i filmski vodič)
Student: Boris Petković
Predmet: Razvoj smartfon-aplikacija
Verzija: 1.0

1. UVOD
1.1 Svrha
Ovaj dokument opisuje softverske zahtjeve za razvoj mobilne aplikacije ScenaBL. Aplikacija omogućava korisnicima praćenje kulturnih dešavanja (pozorišne predstave i bioskopske projekcije), rezervaciju karata, te ocjenjivanje i komentarisanje odgledanih sadržaja. 
Cilj aplikacije je da omogući korisnicima:
- Pregled aktuelnog repertoara,
- Rezervaciju karata za predstave i filmove,
- Vođenje ličnih listi ("Želim gledati", "Odgledano"),
- Ocjenjivanje i recenziranje sadržaja,
- Organizatorima (ustanovama) kreiranje i upravljanje repertoarom.

1.2 Opseg
Aplikacija funkcioniše kao klijent-server sistem za Android uređaje (Kotlin, Android Studio). Koristi Firebase za autentifikaciju, dok se podaci o predstavama, rezervacijama i recenzijama čuvaju u relacionoj bazi (PostgreSQL) posredstvom Node.js REST API-ja.

1.3 Definicije i skraćenice
Korisnik / Gledalac: Registrovani korisnik aplikacije koji rezerviše karte i ostavlja recenzije.
Ustanova (Organizator): Lice/Nalog koje predstavlja pozorište ili bioskop i upravlja repertoarom.
Izvođenje (Događaj): Entitet koji predstavlja konkretan termin predstave ili filma.
Rezervacija: Proces zauzimanja mjesta za određeno izvođenje.
REST API: Arhitektura za komunikaciju klijent-server.
JSON: Format razmjene podataka.

2. OPŠTI OPIS
2.1 Perspektiva proizvoda
- Klijentska aplikacija: Android aplikacija razvijena u Kotlin jeziku.
- Backend: Node.js i Express REST API.
- Baza podataka: PostgreSQL.
- Autentifikacija: Firebase (Auth).
- Komunikacija: Retrofit (HTTP/JSON), Glide (učitavanje slika), ImgBB API (upload slika).

2.2 Klase korisnika i karakteristike
- Gost: Može pregledati repertoar i čitati recenzije bez registracije.
- Registrovani gledalac: Može rezervisati karte, dodavati predstave u lične liste, te ostavljati ocjene i komentare.
- Ustanova (Organizator): Dodaje nove predstave/filmove, zakazuje izvođenja (datumi i vrijeme), prati broj rezervacija i otkazuje termine.
- Administrator: Upravlja nalozima ustanova i moderiše neprikladne recenzije.

2.3 Radno okruženje
- Mobilni uređaji: Android 14.0 (API 36) i novije verzije.
- Internet konekcija: Obavezna za pretragu, rezervacije i učitavanje slika.
- Server i baza: Node.js 18+ sa Express 4+; PostgreSQL 14+.

2.4 Ograničenja sistema
- Aplikacija radi isključivo na Android platformi.
- Offline funkcionalnosti su ograničene na prikaz posljednje učitanog repertoara (keš).
- Sve slike se uploaduju na ImgBB servis.

3. FUNKCIONALNI ZAHTJEVI
3.1 Registracija i prijava
- Registrovani korisnici i ustanove kreiraju nalog unosom imena, e-maila, korisničkog imena i lozinke.
- Validacija podataka pri registraciji (jedinstven email).
- Prijava pomoću emaila i lozinke (Firebase Auth).
- Gost pristup omogućava samo pregled (Read-only režim).

3.2 Upravljanje profilom
- Pregled i izmjena profila: ime, prezime, profilna slika.
- Podešavanje ličnih interesovanja (omiljeni žanrovi: Komedija, Drama, Triler...).

3.3 Pregled i pretraga repertoara
- Lista izvođenja: Prikaz aktuelnih predstava i filmova (naziv, slika, ustanova, vrijeme, prosječna ocjena).
- Detaljan prikaz: Sinopsis, glumačka postava, režiser, preostali broj mjesta i lista recenzija korisnika.
- Pretraga i filtriranje: Po nazivu, datumu (pomoću DatePicker-a) ili žanru.

3.4 Sačuvani sadržaji i liste
- Korisnik može dodati naslov u lične liste:
  - Želim gledati
  - Odgledano
- Ove liste su vidljive u posebnom tabu unutar korisničkog profila.

3.5 Rezervacija karata
- Korisnik može rezervisati mjesto unosom željenog broja karata.
- Sistem provjerava kapacitet (max_mesta - rezervisano).
- Mogućnost otkazivanja rezervacije najkasnije 2 sata prije početka.

3.6 Recenzije i ocjene
- Korisnik može ocijeniti predstavu/film (1–5 zvjezdica) i ostaviti tekstualni komentar (do 500 karaktera) samo ako se naslov nalazi u njegovoj listi "Odgledano".
- Prikaz prosječne ocjene na detaljima naslova.

3.7 Upravljanje repertoarom (Za Ustanove)
- Kreiranje naslova (naziv, opis, trajanje, slika).
- Dodavanje konkretnih termina izvođenja (datum, vrijeme, sala, kapacitet, cijena karte).
- Pregled broja rezervacija po terminu.

4. ZAHTJEVI SPOLJNIH INTERFEJSA
4.1 Korisnički interfejsi (Activity/Fragmenti)
- MainActivity – Početni ekran (login/registracija/gost).
- HomeFragment – Lista aktuelnog repertoara.
- SearchFragment – Pretraga i filtriranje.
- TitleDetailsActivity – Detalji predstave/filma, recenzije i dugme za rezervaciju.
- MyListsFragment – Prikaz "Želim gledati" i "Odgledano".
- OrganizerDashboardActivity – Interfejs za ustanove (kreiranje termina i pregled rezervacija).

4.2 Softverski interfejsi (API)
Glavni REST endpointi (primjeri):
- POST /login – Prijava korisnika.
- GET /repertoar – Lista aktuelnih izvođenja.
- GET /naslov/{id} – Detalji naslova i recenzije.
- POST /rezervacija – Kreiranje rezervacije.
- PATCH /rezervacija/{id}/otkazi – Otkazivanje.
- POST /recenzije – Dodavanje ocjene i komentara.
- POST /liste – Dodavanje naslova u listu (Odgledano/Želim gledati).
- POST /izvodjenje – Dodavanje novog termina (Samo za Ustanove).

4.3 Format podataka
JSON. Primjer izvođenja:
{
  "id": 105,
  "naslov": "Narodni Poslanik",
  "ustanova": "Narodno pozorište RS",
  "datum": "2026-09-15T20:00:00",
  "kapacitet": 300,
  "rezervisano": 120,
  "cijena": 15.0,
  "prosjecna_ocjena": 4.8
}

5. NEFUNKCIONALNI ZAHTJEVI
5.1 Performanse
- Lista repertoara se mora učitati za manje od 1,5 sekundi na stabilnoj mreži.
- Pretraga i filtriranje moraju biti responzivni (< 1s).

5.2 Sigurnost
- Autentifikacija se vrši isključivo preko Firebase Auth servisa (sigurno čuvanje lozinki).
- Svi mrežni pozivi idu preko HTTPS/TLS protokola.

5.3 Pouzdanost i dostupnost
- Aplikacija koristi ViewModel i lokalno keširanje kako bi prikazala barem ranije učitan repertoar u slučaju privremenog gubitka mreže.

6. DODACI
6.1 ER Model baze podataka (Tabele)
- Korisnik (id, ime, prezime, email, uloga, profile_image_url)
- Naslov (id, ime_naslova, opis, reziser, trajanje, slika_url, zanr_id)
- Izvodjenje (id, naslov_id, ustanova_id, datum_vrijeme, kapacitet, cijena)
- Rezervacija (id, korisnik_id, izvodjenje_id, broj_karata, status, datum_rezervacije)
- Recenzija (id, korisnik_id, naslov_id, ocjena, komentar, datum)
- Korisnicka_Lista (korisnik_id, naslov_id, tip_liste [zelim_gledati, odgledano])