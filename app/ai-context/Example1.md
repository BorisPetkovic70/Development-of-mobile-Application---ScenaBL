Software Requirements Specifications (SRS)
Naziv aplikacije: Book Club
Student:
Ime i prezime: Ana Kondić
Broj indeksa: 104/22
Predmet: Razvoj-smartfon aplikacija
Verzija: 2.0

1. UVOD
1.1 Svrha
Ovaj dokument opisuje funkcionalne i nefunkcionalne zahtjeve za mobilnu aplikaciju Book Club.
Cilj aplikacije je da omogući korisnicima:
 Kreiranje i upravljanje klubova za čitanje,
 Praćenje pročitanih i željenih knjiga,
 Dijeljenje utisaka sa članovima kluba,
 Ocjenjivanje i recenziranje knjiga
1.2 Opseg
Aplikacija se razvija kao mobilna aplikacija za Android uređaje korišćenjem Kotlin jezika i Android
Studija. Koristi Firebase za autentifikaciju, skladištenje podataka i real-time sinhronizaciju.
1.3 Definicije i skraćenice
Termin Opis
Korisnik Registrovani korisnik aplikacije
Organizator Lice koje kreira i upravlja klubovima
Klub Entitet u sistemu koji sadrži informacije o grupi za čitanje
Knjiga Entitet u sistemu koji sadrži informacije o knjizi
Rezervacija Proces pridruživanja korisnika klubu ili dodavanje knjige u klub
REST API Arhitektura za komunikaciju klijent-server
FCM Firebase Cloud Messaging, servis za push notifikacije
JSON Format razmjene podataka

2. OPŠTI OPIS
2.1 Korisnici sistema
 Obični korisnici: mogu kreirati profile, pregledati knjige, pridružiti se ili kreirati klubove,
dodavati knjige u klub, komentarisati i ocijenjivati.
 Vlasnici klubova: pored osnovnih funkcionalnosti, mogu brisati klub, upravljati članovima
i odobravati dodavanje knjiga u klub.
2.2 Funkcionalnosti sistema
Aplikacija treba da podržava sledeće funkcionalnosti:
 Registracija i prijava korisnika preko e-maila
 Kreiranje i uređivanje korisničkog profila (ime, e-mail, lozinka, profilna slika).
 Pregled i pretraga knjiga u bazi ili unutar klubova.
 Kreiranje i upravljanje klubovima za čitanje: dodavanje članova, dodavanje knjiga u klub,
pregled članova i knjiga.
 Ocjenjivanje i recenzija knjiga unutar klubova.
2.3 Radno okruženje
 Mobilni uređaji: Android 14.0 (API 36) i novije verzije.
 Internet konekcija: Obavezna za većinu funkcionalnosti (pretraga knjiga, učlanjenje u
klubove, notifikacije.
 Server i baza podataka: Node.js 18+ sa Express 4+ za back-end i PostgreSQL 14+ za
pohranu korisničkih podataka, klubova i recenzija
 Klijent: Android aplikacija koristi:
o Retrofit za REST API pozive prema sopstvenom serveru i eksternim servisima.
o Glide za učitavanje slika.
o ImgBB API za upload korisničkih slika profila i slika klubova.
o Google Books API za pretragu i prikaz informacija o knjigama (naslov, autor, opis,
korica).
2.4 Ograničenje sistema
 Aplikacija radi isključivo na Android platformi.
 Offline funkcionalnosti ograničene na prikaz prethodno keširanih događaja.
 Sve slike se uploaduju na ImgBB. Podržani formati: .png, .jpg, .jpeg. Maksimalna veličina
slike zavisi od ograničenja ImgBB servisa.

3. FUNKCIONALNI ZAHTJEVI
3.1 Registracija i prijava
 Registrovani korisnici kreiraju nalog unosom imena, prezimena, e-maila, korisničkog
imena i lozinke.
 Validacija podataka pri registraciji (jedinstven email i username).
 Prijava pomoću korisničkog imena ili emaila + lozinke.
 Gost može koristiti aplikaciju bez naloga, ali nema mogućnost dodavanja knjiga,
komentarisanja ili kreiranja klubova.
3.2 Upravljanje profilom
 Pregled i izmjena profila: ime, prezime, email, lozinka, profilna slika.
 Podešavanje ličnih interesovanja (žanrovi knjiga ili kategorije klubova).
3.3 Pregled i pretraga knjiga
 Lista knjiga: naziv, autor, žanr, opis, ocjena, naslovna slika.
 Detaljan prikaz knjige: sažetak, ocjene i recenzije, preporuke sličnih knjiga.
 Pretraga knjiga: po nazivu ili ključnim riječima, uz podršku Google Books API za dohvat
dodatnih podataka o knjigama.
3.4 Klubovi i zajednice
 Kreiranje i uređivanje klubova: naziv, opis, slika, pravila članstva.
 Pridruživanje postojećim klubovima.
 Pregled članova kluba i dodavanje knjiga u zajedničke liste.
 Komentarisanje i ocjenjivanje knjiga unutar kluba.
 Push obavještenja o novim dodacima knjiga, komentarima i događajima u klubu.
3.5 Sačuvane knjige i liste
 Korisnik može dodati knjige u lične liste:
o Pročitane knjige
o Želim pročitati
o Trenutno čitam
 Lista sačuvanih knjiga dostupna u posebnoj sekciji profila.
3.6 Recenzije i ocjene
 Ocjena knjige: 1–5 zvjezdica

 Komentar/recenzija: do 500 karaktera
 Jedna recenzija po korisniku po knjizi
 Pregled prosječne ocjene knjige u listama i klubovima

4. ZAHTJEVI SPOLJNIH INTERFEJSA
4.1 Korisnički interfejsi (ekrani i fragmenti)
Activity-i:
 MainActivity – početni ekran, login/registracija/gost pristup
 SignUpActivity – registracija korisnika
 EditProfileActivity – uređivanje korisničkog profila
 BookDetailsActivity – detalji knjige
 BooksSearchActivity – pretraga knjiga
 ClubDetailsActivity – detalji kluba
 HomeActivity – glavni ekran nakon prijave
Fragmenti:
 HomeFragment – prikaz glavnih opcija i sažetka (npr. preporučene knjige, najnoviji
klubovi)
 AvailableClubsFragment – lista dostupnih klubova
 MyClubsFragment – klubovi kojima je korisnik pridružen
 ClubsFragment – pregled svih klubova
 CreateClubFragment – kreiranje novog kluba
 MzBooksFragment – lista mojih knjiga (pročitane/željene)
 BookListFragment – lista knjiga unutar kluba ili rezultata pretrage
 ReviewsFragment – prikaz komentara i ocjena za knjigu
 ProfileFragment – prikaz profila korisnika
 AboutFragment – informacije o aplikaciji
4.2 Hardverski interfejsi

 Internet konekcija (Wi-Fi/4G) za dohvat podataka o knjigama i sinhronizaciju sa Firebase-
om i Google Books API-jem.

 Opcionalno: kamera za upload profilne slike ili fotografije korica knjiga.
4.3 Softverski interfejsi (API)
 Glavni REST endpointi (primjeri):
o POST /login – prijava korisnika.

o POST /register – registracija korisnika.
o GET /books – lista svih knjiga iz baze ili Google Books API.
o GET /books/{id} – detalji knjige.
o POST /books – dodavanje knjige u lične liste.
o POST /clubs – kreiranje kluba.
o GET /clubs – lista dostupnih klubova.
o GET /clubs/{id} – detalji kluba, članovi, knjige.
o POST /clubs/{id}/join – pridruživanje klubu.
o POST /reviews – dodavanje recenzije/ocjene knjige.
o GET /notifications/{userId} – dohvat notifikacija korisnika.

4.4 Format podataka
 JSON.
 Primjer knjige:
{
"id": 1,
"title": "Novels",
"author": "Jane Doe",
"genre": "Fiction",
"description": "A collection of engaging novels for book lovers.",
"rating": 4.5,
"cover_url": "https://example.com/novels_cover.jpg"
}
 Primjer kluba:
{
"id": 1,
"name": "Literary Lovers",
"description": "A club for those passionate about classic and modern literature.",
"members_count": 15,
"books": [1, 2, 3],
"cover_url": "https://example.com/club_cover.jpg"
}

5. NEFUNKCIONALNI ZAHTJEVI
5.1 Performanse
 Lista knjiga i klubova se mora učitati za manje od 1,5 sekundi na stabilnoj 4G vezi.
 Pretraga i filtriranje knjiga ili klubova mora biti responzivno (<1 s).

 Server i Firebase backend moraju podržavati minimalno 500 istovremenih zahtjeva.
5.2 Sigurnost
 Lozinke korisnika se čuvaju hashovane (Firebase Auth).
 Svi podaci u transportu koriste TLS/HTTPS.
 Osjetljivi podaci korisnika (ime, email, profilna slika, istorija čitanja) se čuvaju šifrovano
(Firebase enkripcija).
5.3 Pouzdanost i dostupnost
 Backend (Firebase ili sopstveni server) dostupan ≥ 99% vremena.
 U slučaju nedostupne internetske veze, aplikacija prikazuje posljednji keširani sadržaj
(lista knjiga, klubova i korisnički podaci) kako bi omogućila nesmetan pregled.
5.4 Upotrebljivost
 Jednostavan i pregledan interfejs sa jasno označenim opcijama: prijava, registracija, profil,
pretraga knjiga i klubova.
 Liste knjiga i klubova se automatski osvježavaju, omogućavajući brzu interakciju i
filtriranje informacija.
 Aplikacija podržava intuitivnu navigaciju kroz Activity-e i Fragmente, uključujući kartice,
RecyclerView i bottom navigation gdje je potrebno.
5.5 Skalabilnost i održavanje
 Aplikacija je razvijena koristeći MVVM arhitekturu, što olakšava održavanje i dodavanje
novih funkcionalnosti.
 Komponente su modularne, sa jasno odvojenim View, ViewModel i Model slojevima.

6. DODACI
6.1 Žanrovi knjiga
Aplikacija podržava raznovrsne žanrove knjiga kako bi zadovoljila interesovanja široke baze
korisnika. Podržani žanrovi uključuju:
 Klasična književnost
 Savremena fikcija
 Naučna fantastika
 Fantazija
 Triler i misterija

 Ljubavni romani
 Istorijski romani
 Biografije i memoari
 Psihologija
 Filozofija
 Dječija i omladinska književnost
 Poezija
 Eseji i kritike
 Publicistika i društvene nauke
Korisnici mogu filtrirati i pretraživati knjige po žanru, kao i podešavati svoje interese radi
personalizovanih preporuka.
6.2 Pravila za ocjenivanje
 Ocjena: 1-5 zvjezdica
 Komentar: do 500 karaktera
 Jedna recenzija po korisniku po knjizi