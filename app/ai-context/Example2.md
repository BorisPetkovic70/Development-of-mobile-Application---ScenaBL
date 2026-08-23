SOFTWARE REQUIREMENTS SPECIFICATION (SRS)
Naziv projekta: Događaji u Banja Luci
Predmet: Razvoj smartfon-aplikacija
Verzija: 2.0
Autor: Tara Mačković

1. UVOD
1.1 Svrha
Ovaj dokument definiše softverske zahtjeve za razvoj mobilne aplikacije Događaji u
Banja Luci. Aplikacija omogućava korisnicima pregled i filtriranje aktuelnih događaja u
gradu (koncerti, sportski događaji, izložbe, radionice i sl.), registraciju i prijavu,
rezervaciju ulaznica, personalizovana obavještenja, kao i mogućnost za organizatore da
kreiraju i upravljaju događajima. Dokument jasno opisuje funkcionalnosti, ponašanje i
ograničenja sistema radi uspješnog razvoja i implementacije.
1.2 Opseg
Aplikacija funkcioniše kao klijent-server sistem:
• Korisnici: mogu pretraživati događaje, filtrirati ih po kategoriji, datumu i lokaciji,
sačuvati ih ili rezervisati.
• Organizatori: mogu kreirati, uređivati i brisati događaje, pratiti rezervacije i
primati obavještenja.
• Administratori: putem panela upravljaju bazom događaja i korisnicima.
• Gost: ima mogućnost pregleda događaja bez registracije.

1.3 Definicije i skraćenice

Termin Opis
Korisnik Registrovani korisnik aplikacije
Organizator Lice koje kreira i upravlja događajima

Admin Administrator sistema
Dogadjaj Entitet u sistemu koji sadrži informacije o manifestaciji
Rezervacija Proces prijave korisnika na događaj
REST API Arhitektura za komunikaciju klijent–server
FCM Firebase Cloud Messaging, servis za push notifikacije
JSON Format razmjene podataka

2. OPŠTI OPIS

2.1 Perspektiva proizvoda
• Klijentska aplikacija: Android aplikacija razvijena u Kotlin jeziku.
• Backend: Node.js i Express REST API.
• Baza podataka: PostgreSQL.
• Autentifikacija i notifikacije: Firebase (Auth + FCM).
• Komunikacija: Retrofit (HTTP/JSON).
2.2 Klase korisnika i karakteristike
• Gost: pregled i filtriranje događaja bez registracije.
• Registrovani korisnik: rezervacije, čuvanje događaja, podešavanje interesa i
obavještenja, pregled profila.
• Organizator: kreiranje događaja, uređivanje, pregled rezervacija, dashboard
(statistika).
• Administrator: upravljanje događajima i korisnicima (odobravanje, brisanje).
2.3 Radno okruženje
• Mobilni uređaji: Android 14.0 (API 36) i novije verzije.
• Internet konekcija obavezna za većinu funkcionalnosti.
• Server: Node.js 18+, Express 4+, PostgreSQL 14+.
• Klijent koristi Retrofit za pozivanje API-ja i Glide za učitavanje slika i ImgBB API za
upload korisničkih i organizatorskih slika.

2.4 Ograničenja sistema
• Aplikacija radi isključivo na Android platformi.
• Offline funkcionalnosti ograničene na prikaz prethodno keširanih događaja.
• Sve slike se uploaduju na ImgBB. Podržani formati: .png, .jpg, .jpeg. Maksimalna
veličina slike zavisi od ograničenja ImgBB servisa.

3. FUNKCIONALNI ZAHTJEVI

3.1 Registracija i prijava

• Registrovani korisnici i organizatori kreiraju nalog unosom imena, prezimena, e-
maila, korisničkog imena, lozinke i opcionalno broja telefona.

• Validacija podataka prilikom registracije (jedinstven email i username).
• Prijava pomoću korisničkog imena ili emaila + lozinke.
• Gost može koristiti aplikaciju bez naloga, ali nema mogućnost rezervacija i
personalizacije.
3.2 Upravljanje profilom
• Pregled i izmjena profila: ime, prezime, email, telefon, lozinka, profilna slika.
• Aktivacija/deaktivacija obavještenja.
• Podešavanje ličnih interesovanja (kategorije događaja).
3.3 Pregled događaja
• Lista događaja: naziv, datum, lokacija, cijena, status, slika.
• Detaljan prikaz: opis događaja, broj preostalih mjesta, organizator, kategorija.
• Dinamičko filtriranje:
- po datumu (danas, sutra, ova sedmica, vikend, prilagođeni raspon – pomoću
DatePicker-a),
- po kategorijama (check-box lista učitana sa servera),
- po cijeni (mogućnost prikaza samo besplatnih događaja).
• Sortiranje događaja:
- po nazivu (A–Z),
- po datumu održavanja.
• Pretraga događaja: po nazivu ili ključnim riječima.

3.4 Sačuvani događaji
• Registrovani korisnici mogu dodati događaje u listu sačuvanih.
• Sačuvani događaji dostupni u posebnoj sekciji.
3.5 Rezervacija događaja
• Korisnik može rezervisati događaj unosom broja ulaznica.
• Sistem validira raspoloživost (max_participants – rezervisani).
• Potvrđena rezervacija se čuva u tabeli rezervacija.
• Korisnik i organizator dobijaju push obavještenje.
• Korisnik može otkazati rezervaciju → status = „otkazana“.
3.6 Upravljanje događajima (organizator)
• Kreiranje događaja: naziv, datum, opis, cijena, max učesnika, kategorija, slika.
• Uređivanje i brisanje događaja.
• Dashboard: broj kreiranih događaja, rezervacija i ukupna zarada.
3.7 Notifikacije
• Push obavještenja putem FCM.
• Korisnici: podsjetnik 24h prije događaja, potvrda rezervacije, otkazivanje
događaja.
• Organizatori: obavještenje o novim rezervacijama i otkazivanjima.
3.8 Administratorske funkcije
• Pregled svih događaja i korisnika.
• Otkazivanje i brisanje događaja.
• Upravljanje organizatorima i korisnicima.

4. ZAHTJEVI SPOLJNIH INTERFEJSA
4.1 Korisnički interfejsi (ekrani)
• MainActivity – početna (login, registracija, gost pristup).
• LoginActivity / RegisterActivity – autentifikacija.
• UserDashboardActivity – lista, sortiranje i filtriranje događaja.

• EventDetailsActivity – detalji i rezervacija događaja.
• UserSavedActivity / UserEventsActivity – sačuvani i rezervisani događaji.
• PersonalInterestsActivity – podešavanje interesovanja.
• UserAccountActivity / OrgAccountActivity – profil.
• OrgDashboardActivity / OrgEventsActivity / OrgCreateEventActivity /
OrgEditEventActivity – organizatorske funkcije.
• Admin panel (web) – upravljanje događajima i korisnicima.
• NotificationsActivity, SettingsActivity, HelpActivity, PrivacyActivity,
AccessibilityActivity – dodatne opcije.
4.2 Hardverski interfejsi
• GPS modul za prikaz lokacija događaja.
• Internet konekcija (Wi-Fi/4G).
4.3 Softverski interfejsi (API)
Glavni REST endpointi (primjeri):
• POST /login – prijava korisnika.
• POST /register – registracija korisnika/organizatora.
• GET /dogadjaj – lista događaja.
• POST /dogadjaj – kreiranje događaja (organizator).
• PUT /dogadjaj/{id} – ažuriranje događaja.
• DELETE /dogadjaj/{id} – brisanje događaja.
• POST /rezervacija – rezervacija događaja.
• PATCH /rezervacija/{korisnikID}/{dogadjajID}/otkazi – otkazivanje
rezervacije.
• GET /kategorija – lista kategorija.
• POST /sacuvaj – sačuvaj događaj.
• GET /notifikacije/korisnik/{id} – notifikacije korisnika.
• GET /organizator/{id}/dashboard – statistika organizatora.
4.4 Format podataka
• JSON.
• Primjer događaja:
{
"id": 1,
"name": "Koncert",

"date": "2025-09-30T20:00:00",
"location": "Banja Luka, Kastel",
"description": "Veliki muzički događaj",
"status": "aktivno",
"max_participants": 500,
"price": 10,
"kategorijaid": 2
}

5. NEFUNKCIONALNI ZAHTJEVI
5.1 Performanse
• Lista događaja se mora učitati za za manje od 1.5 sekundi na stabilnoj 4G vezi.
• Server mora podržavati min. 500 istovremenih zahtjeva.
5.2 Sigurnost
• Lozinke se čuvaju hashovane (bcrypt).
• Svi podaci u transportu moraju koristiti TLS.
• Osjetljivi podaci (PII) se šifruju u bazi (AES-256).
• Sesije ističu nakon 30 minuta neaktivnosti.
5.3 Pouzdanost i dostupnost
• Server je dostupan ≥ 99% vremena, čime se osigurava kontinuirani rad
aplikacije.
• U slučaju nedostupne internetske veze, aplikacija prikazuje posljednji keširani
sadržaj kako bi korisnik mogao nastaviti pregled događaja.
5.4 Upotrebljivost
• Jednostavan i pregledan interfejs sa jasno označenim opcijama za prijavu,
registraciju i pregled događaja.
• Liste događaja se automatski osvježavaju, što omogućava brzu interakciju i
filtriranje informacija.

6. DODACI
6.1 ER model baze podataka
• Korisnik
(id, name, surname, email, phone_number, username, password,
profileimageurl, notifications, fcm_token)
• Organizator
(id, name, surname, email, phone_number, username, password,
profileimageurl, notifications, fcm_token)
• Kategorija
(id, name)
• Korisnik_kategorija
(korisnik_id, kategorija_id)
• Događaj
(id, name, date, location, description, status, max_participants, price,
image_url, organizatorid, kategorijaid)
• Sačuvani_događaji
(korisnikid, dogadjajid)
• Rezervacija
(id, korisnikid, dogadjajid, datum_rezervacije, participants, status)
• Notifikacija
(id, korisnik_id, organizator_id, dogadjaj_id, poruka, datum_vrijeme)