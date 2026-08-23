Aplikacija za Ribolov (SRS)
1. Uvod
1.1 Svrha Dokumenta
Svrha ovog dokumenta je detaljno definisati funkcionalne i nefunkcionalne zahtjeve za mobilnu aplikaciju za ribolov. Dokument služi kao osnova za razvojni tim, omogućavajući jasno razumijevanje obima, funkcionalnosti i očekivanog ponašanja aplikacije.
1.2 Obim Proizvoda
Mobilna aplikacija za ribolov ima za cilj da pruži sveobuhvatno digitalno rješenje za ribolovce, omogućavajući im da prate svoje ulove, pronalaze lokacije, prate vremenske uslove i dijele iskustva sa zajednicom. Aplikacija će biti dostupna na Android uredjajima.
1.3 Definicije, Akronimi i Skraćenice
•	SRS: Software Requirements Specification (Specifikacija softverskih zahtjeva)
•	GPS: Global Positioning System
•	UI: User Interface (Korisnički interfejs)
•	UX: User Experience (Korisničko iskustvo)
•	API: Application Programming Interface
•	AI: Artificial Intelligence
•	Push Notifikacije: Obavještenja koja se šalju korisnicima direktno na mobilni uređaj
•	Frontend: Dio aplikacije sa kojim korisnik direktno interaguje
•	Backend: Server-side dio aplikacije koji upravlja podacima i logikom
1.4 Pregled
Ostatak dokumenta je organizovan na sljedeći način:
•	Opšti opis: Visoki pregled funkcija, korisničkih karakteristika i ograničenja.
•	Specifični zahtjevi: Detaljan opis funkcionalnih i nefunkcionalnih zahtjeva.
________________________________________
2. Opšti Opis



2.1 Perspektiva Aplikacije
Aplikacija je samostalni mobilni sistem dizajniran za krajnje korisnike – ribolovce. Ne zavisi od drugih primarnih softverskih sistema, ali će se integrisati sa eksternim servisima kao što su GPS, servisi za vremensku prognozu i eventualno mape (npr. Google Maps API).
2.2 Korisničke Karakteristike
Ciljani korisnici su pojedinci koji se bave ribolovom na amaterskom i poluprofesionalnom nivou. Korisnici će moći da:
•	Evidentiraju detalje o svojim ulovima.
•	Prate statistiku ulova.
•	Označavaju i istražuju ribolovne lokacije.
•	Dobiju relevantne informacije o vremenskim uslovima.
2.3 Opšta Ograničenja
•	Tehnička ograničenja: Kompatibilnost sa Android sistemom (minimalne verzije OS-a će biti definisane kasnije).
•	Performanse: Aplikacija mora biti brza i responsivna, sa minimalnim kašnjenjem prilikom učitavanja podataka i interakcije.
•	Sigurnost: Zaštita korisničkih podataka (posebno ličnih i lokacijskih).
________________________________________
3. Specifični Zahtjevi
3.1 Funkcionalni Zahtjevi
3.1.2 Evidencija Ulova
•	REQ-CATCH-001: Korisnik mora imati mogućnost dodavanja novog ulova. 
o	REQ-CATCH-001a: Unos vrste ribe.
o	REQ-CATCH-001b: Unos težine ulova (kg/g).
o	REQ-CATCH-001c: Unos dužine ulova (cm/mm).
o	REQ-CATCH-001h: Mogućnost dodavanja jedne ili više fotografija ulova.
•	REQ-CATCH-002: Korisnik mora imati mogućnost pregleda svih svojih ulova.
•	REQ-CATCH-003: Korisnik mora imati mogućnost brisanja ulova.
3.1.3 Dnevnik i Statistika Ulova
•	REQ-LOG-001: Aplikacija mora prikazivati sumarnu statistiku ulova (ukupni broj ulova, ukupna težina).
•	REQ-LOG-002: Aplikacija mora prikazivati grafičke prikaze ulova (npr. broj ulova po vrsti ribe, ulov po mjesecu, najuspješniji mamac).
•	REQ-LOG-003: Aplikacija mora automatski prepoznavati i prikazivati lične rekorde (npr. najveća riba određene vrste).
3.1.4 Lokacije za Ribolov (Mape)
•	REQ-LOC-001: Korisnik mora imati mogućnost dodavanja nove ribolovne lokacije na mapi. 
o	REQ-LOC-001a: Unos naziva lokacije.
o	REQ-LOC-001b: Unos opisa lokacije (npr. vrste riba, specifičnosti terena).
o	REQ-LOC-001c: Označavanje lokacije putem GPS-a ili ručno na mapi.
o	REQ-LOC-001d: Mogućnost dodavanja fotografija lokacije.
•	REQ-LOC-002: Korisnik mora imati mogućnost pregleda svih sačuvanih lokacija na interaktivnoj mapi.
•	REQ-LOC-003: Korisnik mora imati mogućnost navigacije do sačuvane lokacije putem integracije sa eksternim map servisima (npr. Google Maps).
•	REQ-LOC-004: Aplikacija treba da omogući pretraživanje javnih ribolovnih lokacija (ako je dostupna baza podataka).
•	REQ-LOC-005: Aplikacija mora prikazivati detalje o lokaciji (vrste ribe, preporučeni mamci, pravila ribolova ako su dostupna).
3.1.5 Vremenska Prognoza za Ribolovce
•	REQ-WEATHER-001: Aplikacija mora prikazivati trenutnu vremensku prognozu za korisnikovu trenutnu lokaciju.
3.1.6 Inventar Opreme
•	REQ-INV-001: Korisnik mora imati mogućnost unosa i praćenja vlastite ribolovne opreme (štapovi, mašinice, varalice, sitni pribor).
3.2 Nefunkcionalni Zahtjevi
3.2.1 Sigurnost
•	NFR-SEC-001: Svi korisnički podaci, uključujući lične informacije i podatke o ulovima, moraju biti šifrovani prilikom prenosa i skladištenja.
•	NFR-SEC-003: Aplikacija mora biti usklađena sa relevantnim zakonima o zaštiti podataka (npr. GDPR).
3.2.2 Upotrebljivost (Usability)
•	NFR-USAB-001: Korisnički interfejs (UI) mora biti intuitivan i jednostavan za korištenje.
•	NFR-USAB-002: Aplikacija mora imati konzistentan dizajn i navigaciju.
3.2.3 Kompatibilnost
•	NFR-COMP-001: Aplikacija mora biti kompatibilna sa Android uređajima verzije 8.0 (Oreo) i novijim.
•	NFR-COMP-003: Aplikacija mora podržavati različite rezolucije ekrana mobilnih uređaja.
3.2.4 Održavanje
•	NFR-MAINT-001: Kod mora biti dobro dokumentovan i modularan za lakše održavanje i buduća proširenja.
________________________________________
4. Dodatni Zahtjevi (Opcije za budući razvoj)
•	Integracija sa AI prepoznavanjem ribe: Korištenje mašinskog učenja za automatsko prepoznavanje vrste ribe sa fotografije.
•	Video tutorijali: Ugrađeni video materijali o ribolovnim tehnikama.
•	Integracija sa pametnim uređajima: Povezivanje sa pametnim vagama ili drugim ribolovnim gadžetima.
•	E-commerce modul: Mogućnost kupovine ribolovne opreme direktno kroz aplikaciju.

