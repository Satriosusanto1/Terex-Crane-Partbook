package com.example.util

import java.util.Locale
import java.util.regex.Pattern

/**
 * High-Accuracy Indonesian-to-English Technical Translator Engine specialized for Heavy Equipment,
 * Mobile & Crawler Cranes (e.g. Liebherr, Tadano, Kato, Kobelco, Grove, Sany, Zoomlion),
 * Mining Machinery (e.g. Caterpillar, Komatsu, Hitachi, Volvo), Mechanical Inspections,
 * Breakdown Root Cause Analysis, and Field Service Reports (FSR).
 *
 * Runs 100% offline on-device without internet connection, perfect for mechanics in remote
 * mining sites (such as Batu Hijau pit / Sumbawa).
 */
object TechnicalTranslator {

    // 1. Common Indonesian Abbreviations & Technical Slang Normalization
    private val slangNormalizationMap = listOf(
        // Common chat / shorthand abbreviations
        "\\byg\\b" to "yang",
        "\\bdgn\\b" to "dengan",
        "\\bkrn\\b" to "karena",
        "\\bkarna\\b" to "karena",
        "\\bsdh\\b" to "sudah",
        "\\bblm\\b" to "belum",
        "\\btdk\\b" to "tidak",
        "\\btak\\b" to "tidak",
        "\\bpd\\b" to "pada",
        "\\butk\\b" to "untuk",
        "\\bsbg\\b" to "sebagai",
        "\\bdr\\b" to "dari",
        "\\bdpt\\b" to "dapat",
        "\\bbbrp\\b" to "beberapa",
        "\\bkmrn\\b" to "kemarin",
        "\\bhr\\b" to "hari",
        "\\btgl\\b" to "tanggal",
        "\\bttd\\b" to "tanda tangan",
        "\\bttd\\.\\b" to "tanda tangan",
        "\\bop\\b" to "operator",
        "\\bopr\\b" to "operator",
        "\\bspv\\b" to "supervisor",
        "\\beng\\b" to "engine",
        "\\bhyd\\b" to "hydraulic",
        "\\btrans\\b" to "transmisi",
        "\\bcyl\\b" to "silinder",
        "\\bassy\\b" to "assembly",
        "\\bbrg\\b" to "bearing",
        "\\bsol\\b" to "solenoid",
        "\\bvlv\\b" to "valve",
        "\\bwh\\b" to "wheel",
        "\\bp2h\\b" to "P2H daily pre-start inspection",
        "\\bpm\\b" to "preventive maintenance",
        "\\bwo\\b" to "work order",
        "\\bpr\\b" to "purchase request",
        "\\bpo\\b" to "purchase order",
        "\\baccu\\b" to "aki",
        "\\blh\\b" to "left hand (kiri)",
        "\\brh\\b" to "right hand (kanan)",
        "\\bfr\\b" to "front (depan)",
        "\\brr\\b" to "rear (belakang)"
    )

    // 2. High-Priority Multi-Word Technical Phrases (Matched and replaced first)
    private val phraseMap = listOf(
        // --- Machine Readiness & Operational Status ---
        "unit siap operasi penuh" to "Unit is fully operational and ready for service",
        "unit siap operasi" to "Unit is ready for operation",
        "unit siap kerja" to "Unit is ready for work",
        "unit siap dioperasikan kembali" to "Unit is ready to be put back into service",
        "unit siap untuk bekerja" to "Unit is ready for active operation",
        "unit layak operasi" to "Unit is serviceable and safe for operation",
        "unit layak beroperasi" to "Unit is fit for operation",
        "unit dalam kondisi baik dan aman" to "Unit is in good and safe operating condition",
        "unit beroperasi normal" to "Unit operates normally",
        "unit beroperasi dengan baik" to "Unit is functioning properly",
        "unit tidak bisa dioperasikan" to "Unit is out of service / non-operational",
        "unit tidak siap operasi" to "Unit is not ready for operation",
        "unit belum siap operasi" to "Unit is not yet ready for operation",
        "unit stop operasi" to "Unit is tagged out / out of service",
        "unit masih dalam perbaikan" to "Unit is currently undergoing repair",
        "unit standby menunggu part" to "Unit is on standby awaiting spare parts",
        "unit standby menunggu" to "Unit is on standby awaiting",
        "unit mengalami breakdown" to "Unit experienced an unscheduled breakdown",
        "unit mengalami mati mendadak" to "Unit experienced sudden engine shutdown",
        "unit mati total" to "Unit completely dead / failed to power on",
        "unit tidak bertenaga" to "Unit experienced low engine power / power loss",
        "unit kurang tenaga" to "Unit lacks engine power (low power)",
        "unit mengalami overheat" to "Unit experienced engine overheating",
        "unit mengalami kebocoran oli hidrolik" to "Unit experienced hydraulic oil leakage",
        "unit mengalami kebocoran oli mesin" to "Unit experienced engine oil leakage",
        "unit mengalami kebocoran solar" to "Unit experienced diesel fuel leakage",
        "unit mengalami kebocoran oli" to "Unit experienced oil leakage",
        "unit mengalami kebocoran" to "Unit experienced leakage",
        "unit mengalami suara abnormal" to "Unit produced abnormal noise",
        "unit mengalami getaran tinggi" to "Unit exhibited excessive vibration",
        "unit mengalami kendala" to "Unit experienced an issue",

        // --- Crane & Heavy Equipment Operation Contexts ---
        "saat operasi lifting" to "during lifting operation",
        "saat mengangkat beban" to "during load lifting operation",
        "saat mengangkat muatan" to "during load handling operation",
        "saat boom extend" to "during boom extension",
        "saat boom retract" to "during boom retraction",
        "saat boom naik" to "during boom hoist up (luffing up)",
        "saat boom turun" to "during boom hoist down (luffing down)",
        "saat swing kanan dan kiri" to "during right and left swing (slewing)",
        "saat swing kanan" to "during right swing (slewing)",
        "saat swing kiri" to "during left swing (slewing)",
        "saat traveling di tanjakan" to "during uphill travel under load",
        "saat traveling di turunan" to "during downhill travel",
        "saat traveling di area tambang" to "during traveling on mine haul road",
        "saat traveling" to "during travel / tramming",
        "saat beban puncak" to "during peak load operation",
        "pada saat start engine" to "during engine startup",
        "pada saat start mesin" to "during engine startup",
        "saat mesin dihidupkan" to "upon starting the engine",
        "pada saat idle" to "during engine idle speed",
        "pada saat rpm tinggi" to "at high engine RPM",
        "pada saat putaran tinggi" to "at high rotational speed",
        "pada saat akselerasi" to "during acceleration",
        "pada saat pengereman" to "during braking",
        "pada saat operasi" to "during active operation",
        "di area tambang" to "at mine site area",
        "di workshop" to "at maintenance workshop",
        "di lapangan" to "in the field",
        "di pit tambang" to "in the mining pit",

        // --- Defect & Inspection Findings ---
        "ditemukan kebocoran oli hidrolik" to "hydraulic oil leakage was identified",
        "ditemukan kebocoran oli mesin" to "engine oil leakage was identified",
        "ditemukan kebocoran oli transmisi" to "transmission oil leakage was identified",
        "ditemukan kebocoran oli gardan" to "differential oil leakage was identified",
        "ditemukan kebocoran oli" to "oil leakage was observed",
        "ditemukan rembesan oli hidrolik" to "hydraulic oil seepage was identified",
        "ditemukan rembesan oli mesin" to "engine oil seepage was identified",
        "ditemukan rembesan oli" to "oil seepage was identified",
        "ditemukan kebocoran solar" to "diesel fuel leakage was identified",
        "ditemukan rembesan solar" to "fuel seepage was identified",
        "ditemukan kebocoran coolant" to "coolant leakage was observed",
        "ditemukan kebocoran air radiator" to "radiator coolant leakage was observed",
        "ditemukan kebocoran udara" to "pneumatic air leakage was detected",
        "ditemukan retakan pada struktur" to "structural cracks were detected on",
        "ditemukan retakan pada" to "cracks were detected on",
        "ditemukan keausan berlebih pada" to "excessive wear was found on",
        "ditemukan keausan pada" to "wear and tear was found on",
        "ditemukan kabel kelistrikan putus" to "severed electrical wiring was found",
        "ditemukan kabel putus" to "broken wiring was detected",
        "ditemukan konektor kendor" to "loose electrical connector was found",
        "ditemukan konektor korosi" to "corroded connector was found",
        "ditemukan baut pengikat kendor" to "loose retaining bolts were identified",
        "ditemukan baut kendor" to "loose bolts were identified",
        "ditemukan baut patah" to "sheared / broken bolts were found",
        "ditemukan baut slek" to "stripped bolt threads were found",
        "ditemukan baut dol" to "stripped bolt threads were found",
        "ditemukan hose hidrolik pecah" to "burst hydraulic hose was found",
        "ditemukan hose pecah" to "burst hose was found",
        "ditemukan hose retak" to "weather-cracked hose was found",
        "ditemukan seal robek" to "torn / damaged hydraulic seal was found",
        "ditemukan seal rusak" to "damaged seal was found",
        "ditemukan o-ring rusak" to "damaged O-ring was found",
        "ditemukan suara abnormal pada mesin" to "abnormal engine knocking noise was detected",
        "ditemukan suara abnormal" to "abnormal noise was observed",
        "ditemukan getaran berlebih" to "excessive vibration was noted",
        "ditemukan kode error pada display" to "diagnostic error code displayed on monitor",
        "ditemukan kode error" to "fault error code was displayed",
        "muncul indikator warning" to "warning indicator illuminates on dashboard",
        "indikator lmi error" to "LMI (Load Moment Indicator) system error",
        "alarm berbunyi terus menerus" to "audible warning alarm sounded continuously",
        "tekanan oli hidrolik drop" to "hydraulic oil pressure dropped below standard",
        "tekanan oli mesin drop" to "engine oil pressure dropped below standard",
        "tekanan oli mesin rendah" to "low engine oil pressure",
        "tekanan angin rem rendah" to "low brake air pressure",
        "suhu oli transmisi tinggi" to "high transmission oil temperature",
        "suhu coolant mesin tinggi" to "high engine coolant temperature (overheat)",
        "suhu oli hidrolik tinggi" to "high hydraulic oil temperature",
        "asap knalpot hitam pekat" to "excessive black exhaust smoke",
        "asap knalpot putih tebal" to "thick white exhaust smoke",
        "asap knalpot biru" to "blue exhaust smoke indicating oil combustion",
        "level oli hidrolik kurang" to "hydraulic oil level is low below MIN mark",
        "level oli mesin kurang" to "engine oil level is low below MIN mark",
        "level air radiator kurang" to "radiator coolant level is low",
        "kawat seling putus" to "broken wire rope strands detected",
        "wire rope ada kawat putus" to "broken strands observed on wire rope",

        // --- Actions Taken / Maintenance Executed ---
        "telah dilakukan penggantian dan pengetesan" to "completed replacement and functional testing",
        "telah dilakukan perbaikan dan pengetesan" to "completed repair and functional testing",
        "telah dilakukan penggantian" to "replacement has been completed",
        "telah dilakukan perbaikan" to "repairs have been carried out",
        "telah dilakukan inspeksi berkala" to "periodic inspection has been conducted",
        "telah dilakukan inspeksi visual" to "visual inspection has been conducted",
        "telah dilakukan inspeksi fungsi" to "functional inspection has been conducted",
        "telah dilakukan inspeksi" to "thorough inspection has been conducted",
        "telah dilakukan pengecekan level oli" to "fluid level check has been performed",
        "telah dilakukan pengecekan" to "checking and inspection have been conducted",
        "telah dilakukan pembersihan" to "cleaning and degreasing have been performed",
        "telah dilakukan penyetelan" to "adjustment has been carried out",
        "telah dilakukan penyetelan clearance" to "clearance adjustment has been carried out",
        "telah dilakukan pengencangan baut" to "bolt re-torquing has been completed",
        "telah dilakukan pengencangan" to "tightening has been completed",
        "telah dilakukan penambahan oli hidrolik" to "hydraulic oil top-up completed",
        "telah dilakukan penambahan oli mesin" to "engine oil top-up completed",
        "telah dilakukan penambahan oli" to "oil replenishment has been completed",
        "telah dilakukan penambahan coolant" to "coolant top-up completed",
        "telah dilakukan pengurasan dan flushing" to "draining and system flushing completed",
        "telah dilakukan pengurasan" to "system draining completed",
        "telah dilakukan kalibrasi sensor" to "sensor calibration has been completed",
        "telah dilakukan kalibrasi lmi" to "LMI system calibration completed",
        "telah dilakukan kalibrasi" to "calibration has been successfully completed",
        "telah dilakukan pengelasan struktur" to "structural welding repair completed",
        "telah dilakukan pengelasan" to "welding repair has been completed",
        "telah dilakukan pengetesan fungsi" to "functional testing has been executed",
        "telah dilakukan pengetesan running" to "running test has been executed",
        "telah dilakukan pengetesan" to "testing has been successfully executed",
        "telah dilakukan uji beban" to "load testing has been conducted",
        "telah dilakukan flushing sistem hidrolik" to "hydraulic system flushing performed",
        "telah dilakukan flushing oli" to "oil flushing has been performed",
        "telah dilakukan greasing pada semua titik" to "greasing performed on all lubrication points",
        "telah dilakukan greasing" to "greasing and lubrication completed",
        "telah dilakukan bleeding sistem rem" to "brake system bleeding completed",
        "telah dilakukan bleeding udara" to "air bleeding performed",
        "sudah diganti dengan yang baru" to "replaced with new genuine OEM component",
        "sudah diganti part baru" to "replaced with new spare part",
        "sudah diperbaiki dan ditest" to "repaired and fully function tested",
        "sudah disetel sesuai standar pabrik" to "adjusted to factory standard specifications",
        "sudah dikencangkan sesuai torsi standar" to "torqued to standard manufacturer specifications",
        "sudah dikencangkan sesuai torsi" to "torqued to manufacturer specifications",
        "sudah dikalibrasi ulang" to "re-calibrated to standard specifications",
        "running test tanpa beban" to "no-load running test",
        "running test dengan beban" to "running test under load",
        "uji coba fungsi normal" to "functional test normal and verified",

        // --- Recommendations & Conclusions ---
        "jadwalkan servis berkala berikutnya" to "Schedule next periodic maintenance service",
        "jadwalkan penggantian seal kit" to "Schedule cylinder seal kit replacement",
        "jadwalkan penggantian part" to "Schedule replacement of required spare parts",
        "jadwalkan inspeksi berkala pada" to "Schedule periodic inspection at",
        "jadwalkan servis berkala" to "Schedule periodic service",
        "pantau kondisi setiap shift" to "Monitor unit condition on every operating shift",
        "pantau level oli hidrolik" to "Monitor hydraulic oil level daily",
        "pantau temperatur mesin" to "Monitor engine operating temperature",
        "monitoring berkala harian" to "Perform daily periodic monitoring",
        "monitoring berkala setiap shift" to "Perform monitoring every shift",
        "monitoring berkala" to "Routine periodic monitoring required",
        "operator wajib melakukan p2h" to "Operator must strictly conduct daily P2H pre-start checks",
        "operator wajib melakukan pengecekan" to "Operator must conduct pre-operational checks",
        "menunggu kedatangan spare part" to "Awaiting delivery of spare parts",
        "menunggu kedatangan part" to "Awaiting spare parts arrival",
        "menunggu approval dari user" to "Pending approval from client supervisor",
        "menunggu approval spv" to "Pending supervisor approval",
        "menunggu jadwal shutdown" to "Scheduled for upcoming site shutdown maintenance",
        "lakukan pengetesan berkala" to "Conduct periodic functional tests",
        "ikuti prosedur keselamatan kerja" to "Strictly follow site occupational health & safety procedures",
        "pastikan tidak ada beban berlebih" to "Ensure no overloading beyond rated load chart capacity",

        // --- Periodic Service Intervals ---
        "servis berkala 250 jam" to "250-Hour Periodic Maintenance Service",
        "servis berkala 500 jam" to "500-Hour Periodic Maintenance Service",
        "servis berkala 1000 jam" to "1,000-Hour Periodic Maintenance Service",
        "servis berkala 2000 jam" to "2,000-Hour Periodic Maintenance Service",
        "servis berkala 4000 jam" to "4,000-Hour Periodic Maintenance Service",
        "servis berkala 5000 jam" to "5,000-Hour Periodic Maintenance Service",
        "perawatan 250 jam" to "250-hour maintenance",
        "perawatan 500 jam" to "500-hour maintenance",
        "perawatan 1000 jam" to "1,000-hour maintenance",
        "perawatan 2000 jam" to "2,000-hour maintenance",
        "program perawatan berkala" to "preventive maintenance program",
        "jadwal perawatan preventif" to "preventive maintenance schedule",
        "inspeksi harian" to "daily pre-start inspection",
        "inspeksi mingguan" to "weekly inspection",
        "inspeksi bulanan" to "monthly inspection",
        "inspeksi tahunan" to "annual certification inspection",
        "uji kelayakan operasi" to "commissioning & operational readiness certification",
        "uji beban lifting" to "crane load testing",
        "inspeksi visual dan fungsi" to "visual and functional inspection",
        "inspeksi visual" to "visual inspection",
        "inspeksi fungsi" to "functional inspection",
        "riwayat kerusakan sebelumnya" to "previous breakdown history",
        "riwayat perbaikan sebelumnya" to "previous repair history",
        "riwayat servis berkala" to "periodic service history",
        "riwayat penggantian komponen" to "component replacement history",
        "catatan breakdown sebelumnya" to "previous breakdown record",
        "catatan perbaikan" to "repair record notes"
    )

    // 3. Comprehensive Technical Dictionary (Compound Terms & Single Words)
    private val technicalDictionary = mapOf(
        // === Heavy Equipment, Crane & Mining Components ===
        "mesin" to "engine",
        "engine" to "engine",
        "oli mesin" to "engine oil",
        "oli hidrolik" to "hydraulic oil",
        "oli transmisi" to "transmission oil",
        "oli gardan" to "differential gear oil",
        "oli rem" to "brake fluid",
        "minyak rem" to "brake fluid",
        "minyak power steering" to "power steering fluid",
        "air radiator" to "radiator coolant",
        "coolant" to "engine coolant",
        "solar" to "diesel fuel",
        "bahan bakar" to "fuel",
        "grease" to "lubricating grease",
        "gemuk" to "lubricating grease",

        // Filters
        "filter oli mesin" to "engine oil filter",
        "filter oli hidrolik" to "hydraulic oil filter",
        "filter oli" to "oil filter",
        "filter solar" to "fuel filter",
        "filter bahan bakar" to "fuel filter",
        "filter udara" to "air filter",
        "filter hidrolik" to "hydraulic filter",
        "filter transmisi" to "transmission filter",
        "water separator" to "fuel-water separator",
        "saringan udara" to "air cleaner element",
        "saringan solar" to "fuel strainer",

        // Hydraulics & Pneumatics
        "sistem hidrolik" to "hydraulic system",
        "tangki hidrolik" to "hydraulic oil reservoir tank",
        "pompa hidrolik" to "hydraulic main pump",
        "pompa steering" to "steering pump",
        "pompa solar" to "fuel feed pump",
        "motor hidrolik" to "hydraulic motor",
        "silinder hidrolik" to "hydraulic cylinder",
        "silinder boom" to "boom telescopic cylinder",
        "silinder luffing" to "derricking / luffing cylinder",
        "silinder outrigger" to "outrigger cylinder",
        "silinder jack" to "outrigger vertical jack cylinder",
        "silinder steering" to "steering cylinder",
        "silinder bucket" to "bucket cylinder",
        "silinder arm" to "arm cylinder",
        "silinder tilt" to "tilt cylinder",
        "selang hidrolik" to "hydraulic hose",
        "selang solar" to "fuel hose",
        "selang radiator" to "radiator hose",
        "selang udara" to "pneumatic air hose",
        "hose hidrolik" to "hydraulic hose",
        "pipa hidrolik" to "hydraulic steel pipe / line",
        "hose fitting" to "hose fitting",
        "nepel" to "nipple fitting",
        "nipel" to "nipple fitting",
        "klem hose" to "hose clamp",
        "klem" to "clamp",
        "katup" to "valve",
        "valve" to "valve",
        "control valve" to "main control valve",
        "relief valve" to "pressure relief valve",
        "solenoid valve" to "solenoid control valve",
        "check valve" to "check valve",
        "pilot valve" to "pilot control valve",
        "proportional valve" to "proportional valve",
        "accumulator" to "hydraulic accumulator",
        "seal kit" to "cylinder seal kit",
        "seal rod" to "rod seal",
        "seal piston" to "piston seal",
        "seal debu" to "wiper dust seal",
        "wiper seal" to "wiper seal",
        "o-ring" to "O-ring",
        "oring" to "O-ring",
        "gasket" to "gasket",
        "paking" to "gasket",

        // Powertrain, Transmission & Brakes
        "transmisi" to "transmission",
        "gearbox" to "gearbox",
        "kopling" to "clutch",
        "kampas kopling" to "clutch disc lining",
        "master kopling" to "clutch master cylinder",
        "booster rem" to "brake booster",
        "gardan" to "differential",
        "gardal" to "differential",
        "propeller shaft" to "drive propeller shaft",
        "kopel" to "drive shaft / universal joint",
        "universal joint" to "universal joint",
        "as roda" to "drive axle shaft",
        "final drive" to "final drive reduction gear",
        "roda" to "wheel",
        "ban" to "tire",
        "velg" to "wheel rim",
        "rem" to "brake",
        "rem tangan" to "parking brake",
        "rem parkir" to "parking brake",
        "rem kaki" to "service brake",
        "kampas rem" to "brake lining / pads",
        "cakram rem" to "brake disc rotor",
        "tromol rem" to "brake drum",
        "caliper rem" to "brake caliper",
        "air dryer" to "air dryer cartridge",
        "kompresor angin" to "air compressor",
        "tabung angin" to "compressed air reservoir tank",

        // Engine & Cooling Systems
        "blok silinder" to "engine cylinder block",
        "kepala silinder" to "cylinder head",
        "piston" to "piston",
        "ring piston" to "piston rings",
        "stang seher" to "connecting rod",
        "kruk as" to "crankshaft",
        "noken as" to "camshaft",
        "klep" to "engine valve",
        "radiator" to "radiator",
        "kipas radiator" to "radiator cooling fan",
        "tali kipas" to "fan belt (V-belt)",
        "v-belt" to "fan drive belt",
        "fan belt" to "fan drive belt",
        "water pump" to "engine water pump",
        "pompa air" to "water pump",
        "thermostat" to "thermostat",
        "oil cooler" to "engine oil cooler",
        "intercooler" to "charge air intercooler",
        "turbocharger" to "turbocharger",
        "turbo" to "turbocharger",
        "manifold" to "manifold",
        "knalpot" to "exhaust muffler system",
        "exhaust" to "exhaust",
        "muffler" to "exhaust muffler",
        "injektor" to "fuel injector",
        "injector" to "fuel injector",
        "nozzle" to "fuel injector nozzle",
        "common rail" to "common rail injection system",
        "busi pemanas" to "glow plug",

        // Electrical & Electronics
        "alternator" to "alternator",
        "dinamo ampere" to "alternator",
        "altenator" to "alternator",
        "starter motor" to "starter motor",
        "dinamo starter" to "starter motor",
        "aki" to "battery",
        "baterai" to "battery",
        "accu" to "battery",
        "kutub aki" to "battery terminal",
        "kepala aki" to "battery terminal clamp",
        "kabel aki" to "battery cable",
        "kabel kelistrikan" to "electrical wiring harness",
        "wiring harness" to "wiring harness",
        "kabel" to "wiring / cable",
        "sekring" to "fuse",
        "fuse box" to "fuse junction box",
        "relay" to "electrical relay",
        "sensor suhu" to "temperature sensor",
        "sensor tekanan" to "pressure sensor / transducer",
        "sensor level" to "level sensor",
        "sensor kecepatan" to "speed sensor",
        "sensor sudut" to "angle sensor",
        "sensor panjang boom" to "boom length sensor",
        "sensor beban" to "load cell sensor",
        "pressure switch" to "pressure switch",
        "lampu kerja" to "work light",
        "lampu sorot" to "floodlight",
        "lampu utama" to "headlight",
        "lampu rem" to "brake stop lamp",
        "lampu sein" to "turn signal lamp",
        "lampu rotary" to "warning beacon strobe light",
        "rotary lamp" to "warning beacon strobe light",
        "sirine" to "warning siren / horn",
        "klakson" to "electric / air horn",
        "wiper" to "windshield wiper",
        "motor wiper" to "windshield wiper motor",
        "kaca spion" to "rearview side mirror",
        "spion" to "side mirror",
        "kaca depan" to "front windshield glass",
        "windshield" to "front windshield",

        // Crane Superstructure & Lifting Attachments
        "teleskopik" to "telescopic section",
        "boom" to "telescopic boom",
        "main boom" to "main telescopic boom",
        "fly jib" to "fly jib lattice extension",
        "jib" to "jib extension",
        "counterweight" to "counterweight ballast",
        "beban penyeimbang" to "counterweight ballast",
        "slewing ring" to "slewing ring / swing bearing",
        "slewing bearing" to "slewing ring bearing",
        "swing bearing" to "swing bearing",
        "swing motor" to "hydraulic swing motor",
        "swing gear" to "swing reduction gearbox",
        "swing brake" to "swing holding brake",
        "winch utama" to "main hoisting winch",
        "winch tambahan" to "auxiliary hoisting winch",
        "winch hoist" to "hoisting winch drum",
        "winch" to "winch hoist",
        "tali seling" to "hoisting wire rope",
        "seling kawat" to "wire rope cable",
        "wire rope" to "hoisting wire rope",
        "katrol" to "sheave / pulley",
        "pulley" to "sheave / pulley",
        "sheave" to "boom tip sheave",
        "hook block" to "main hook block",
        "ball hook" to "auxiliary headache ball hook",
        "kait" to "lifting hook",
        "safety latch" to "hook safety latch",
        "pengunci hook" to "hook safety latch",
        "swivel" to "load swivel",
        "shackle" to "lifting shackle",
        "segel" to "lifting shackle",
        "webbing sling" to "synthetic webbing sling",
        "tali sabuk" to "webbing sling",
        "chain block" to "chain hoist / block",

        // Outriggers & Chassis
        "outrigger" to "stabilizer outrigger",
        "jack outrigger" to "vertical outrigger jack",
        "beam outrigger" to "horizontal outrigger beam",
        "outrigger pad" to "outrigger ground support pad",
        "alas outrigger" to "outrigger support plate / pad",
        "chassis" to "carrier chassis frame",
        "superstructure" to "revolving superstructure",
        "kabin operator" to "operator control cabin",
        "kabin carrier" to "driver carrier cabin",
        "kabin" to "operator cabin",
        "kursi operator" to "operator suspension seat",
        "sabuk pengaman" to "seat safety belt",
        "apar" to "fire extinguisher",
        "ac kabin" to "cabin air conditioning system",

        // Crane Safety Instruments & Computer
        "lmi" to "LMI (Load Moment Indicator)",
        "load moment indicator" to "Load Moment Indicator (LMI)",
        "display monitor" to "operator control display monitor",
        "layar monitor" to "graphic display monitor",
        "anti two block" to "Anti-Two-Block (A2B) limit switch",
        "anti-two-block" to "Anti-Two-Block (A2B) limit switch",
        "a2b" to "Anti-Two-Block (A2B) safety system",
        "limit switch" to "safety limit switch",
        "anemometer" to "wind speed anemometer sensor",
        "load cell" to "strain gauge load cell sensor",
        "joystick" to "hydraulic / pilot control joystick",
        "pedal gas" to "throttle accelerator pedal",
        "pedal rem" to "brake pedal",
        "pedal kopling" to "clutch pedal",
        "pedal" to "control foot pedal",

        // Fasteners, Mechanical & Structural Terms
        "baut" to "bolt",
        "mur" to "nut",
        "ring baut" to "spring / flat washer",
        "ring" to "washer",
        "stud bolt" to "stud bolt",
        "pin boom" to "boom pivot pin",
        "pin" to "pivot pin",
        "bushing" to "bushing sleeve",
        "laher" to "rolling bearing",
        "bearing" to "bearing",
        "grease fitting" to "grease lubrication nipple",
        "nipel gemuk" to "grease lubrication nipple",
        "spring" to "recoil spring / tensioner",
        "per" to "spring",

        // Track & Excavator Components
        "track link" to "track link assembly",
        "track shoe" to "track shoe / pad",
        "roller atas" to "carrier upper roller",
        "roller bawah" to "track lower roller",
        "sprocket" to "drive sprocket",
        "idler" to "front idler wheel",
        "track adjuster" to "track hydraulic adjuster tensioner",
        "bucket" to "excavator bucket",
        "kuku bucket" to "bucket teeth",
        "tooth" to "bucket tooth",
        "cutting edge" to "blade cutting edge",

        // === Defect, Failure & Condition Descriptors ===
        "kebocoran" to "leakage",
        "rembesan" to "seepage",
        "tetesan" to "dripping",
        "kerusakan" to "damage / defect",
        "keausan" to "wear and tear",
        "keausan berlebih" to "excessive wear-down",
        "kelonggaran" to "excessive play / looseness",
        "keretakan" to "cracking",
        "retak" to "cracked",
        "patah" to "broken / fractured / sheared",
        "bengkok" to "bent / deformed",
        "penyok" to "dented",
        "robek" to "torn / ruptured",
        "sobek" to "torn / ruptured",
        "pecah" to "burst / shattered",
        "bocor" to "leaking",
        "bocor halus" to "fine pinhole leak",
        "rembes" to "seeping",
        "netes" to "dripping",
        "muncrat" to "spraying / jetting",
        "aus" to "worn out",
        "termakan" to "eroded / worn down",
        "terkikis" to "eroded / abraded",
        "oblak" to "loose with excessive play",
        "kocak" to "loose with play",
        "goyang" to "unstable / wobbling",
        "kendor" to "loose",
        "kendur" to "loose",
        "longgar" to "loose",
        "slek" to "stripped thread",
        "dol" to "stripped thread",
        "macet" to "jammed / stuck / seized",
        "seret" to "sluggish / dragging",
        "panas" to "overheated / hot",
        "overheat" to "overheated",
        "kebakar" to "burnt",
        "hangus" to "scorched / charred",
        "terbakar" to "burnt",
        "kotor" to "contaminated / dirty",
        "berkerak" to "crusted / scaled",
        "berkarat" to "rusted / corroded",
        "karatan" to "corroded",
        "karat" to "corrosion / rust",
        "korosi" to "corrosion",
        "tersumbat" to "clogged / blocked",
        "buntu" to "clogged",
        "mampet" to "clogged",
        "kurang" to "insufficient / low",
        "habis" to "depleted / empty",
        "kosong" to "empty",
        "drop" to "dropped / dropped below spec",
        "turun" to "decreased / lowered",
        "naik" to "increased / elevated",
        "tinggi" to "high",
        "rendah" to "low",
        "suara abnormal" to "abnormal noise",
        "bunyi abnormal" to "abnormal sound",
        "bunyi knocking" to "knocking noise",
        "bunyi krek krek" to "grinding / rattling noise",
        "bunyi decit" to "squeaking squeal noise",
        "bunyi mendengung" to "humming / whining noise",
        "suara kasar" to "harsh running noise",
        "getaran berlebih" to "excessive vibration",
        "getar kencang" to "severe vibration",
        "getaran" to "vibration",
        "getar" to "vibrating",
        "bising" to "excessive noise",
        "ngebul" to "emitting smoke",
        "asap hitam" to "black exhaust smoke",
        "asap putih" to "white exhaust smoke",
        "asap biru" to "blue exhaust smoke",
        "kurang tenaga" to "low power output",
        "low power" to "low power",
        "ngempos" to "loss of engine compression / low power",
        "lambat" to "sluggish response",
        "lemot" to "sluggish",
        "tidak merespon" to "unresponsive",
        "tidak bisa start" to "engine crank but failed to start",
        "susah hidup" to "hard starting",
        "mati mendadak" to "sudden engine stall / shutdown",
        "mogok" to "breakdown / stalled",
        "error" to "fault error",
        "rusak parah" to "severely damaged",
        "rusak" to "damaged / defective",
        "cacat" to "flawed / defective",
        "abnormal" to "abnormal",
        "normal" to "normal and within spec",

        // === Actions & Engineering Verbs ===
        "mengganti" to "replaced",
        "ganti" to "replaced",
        "diganti" to "replaced",
        "pergantian" to "replacement",
        "penggantian" to "replacement",
        "memperbaiki" to "repaired",
        "perbaiki" to "repaired",
        "diperbaiki" to "repaired",
        "perbaikan" to "repair",
        "memeriksa" to "inspected",
        "mengecek" to "checked / inspected",
        "cek" to "checked",
        "dicek" to "checked",
        "diperiksa" to "inspected",
        "pemeriksaan" to "inspection",
        "pengecekan" to "checking & inspection",
        "membersihkan" to "cleaned",
        "bersihkan" to "cleaned",
        "dibersihkan" to "cleaned",
        "pembersihan" to "cleaning",
        "menyetel" to "adjusted",
        "setel" to "adjusted",
        "disetel" to "adjusted",
        "penyetelan" to "adjustment",
        "mengencangkan" to "tightened / torqued",
        "kencangkan" to "tightened",
        "dikencangkan" to "tightened / torqued to spec",
        "pengencangan" to "tightening",
        "mengelas" to "welded",
        "las" to "welded",
        "dilas" to "welded",
        "pengelasan" to "welding repair",
        "mengetes" to "tested",
        "menguji" to "tested",
        "tes" to "tested",
        "dites" to "tested",
        "diuji" to "tested",
        "pengetesan" to "functional testing",
        "pengujian" to "testing",
        "membongkar" to "disassembled",
        "bongkar" to "disassembled",
        "dibongkar" to "disassembled",
        "pembongkaran" to "disassembly",
        "memasang" to "installed",
        "pasang" to "installed",
        "dipasang" to "installed",
        "pemasangan" to "installation",
        "menguras" to "drained",
        "kuras" to "drained",
        "dikuras" to "drained",
        "pengurasan" to "draining",
        "menambah" to "topped up / replenished",
        "tambah" to "topped up",
        "ditambah" to "topped up",
        "penambahan" to "replenishment / top-up",
        "mengisi" to "filled / refilled",
        "isi" to "filled",
        "diisi" to "filled",
        "pengisian" to "refilling",
        "melumasi" to "lubricated",
        "lumasi" to "lubricated",
        "dilumasi" to "lubricated",
        "pelumasan" to "greasing / lubrication",
        "tap ulang" to "re-threaded / re-tapped",
        "ditap" to "re-tapped",
        "mengkalibrasi" to "calibrated",
        "kalibrasi" to "calibrated",
        "dikalibrasi" to "calibrated",
        "pengkalibrasian" to "calibration",
        "memonitor" to "monitored",
        "pantau" to "monitored",
        "dipantau" to "monitored",
        "pemantauan" to "routine monitoring",
        "menjadwalkan" to "scheduled",
        "jadwalkan" to "scheduled",
        "dijadwalkan" to "scheduled",
        "penjadwalan" to "scheduling",
        "menemukan" to "identified",
        "ditemukan" to "identified / observed",
        "temuan" to "findings",
        "melepas" to "removed / detached",
        "lepas" to "removed",
        "dilepas" to "removed",
        "pelepasan" to "removal",
        "memotong" to "cut",
        "potong" to "cut",
        "dipotong" to "cut",
        "pemotongan" to "cutting",
        "membubut" to "machined / lathed",
        "bubut" to "machined",
        "dibubut" to "machined",
        "pembubutan" to "machining",
        "menyambung" to "connected / spliced",
        "sambung" to "connected",
        "disambung" to "connected / spliced",
        "penyambungan" to "connection",
        "memutus" to "disconnected",
        "putus" to "severed / disconnected",
        "diputus" to "disconnected",
        "pemutusan" to "disconnection",
        "mengunci" to "locked",
        "kunci" to "locked",
        "dikunci" to "locked",
        "penguncian" to "locking",
        "mengukur" to "measured",
        "ukur" to "measured",
        "diukur" to "measured",
        "pengukuran" to "measurement",
        "membuang" to "disposed / drained",
        "buang" to "disposed",
        "dibuang" to "disposed",
        "pembuangan" to "disposal",
        "flushing" to "system flushing",
        "bleeding" to "air bleeding",
        "greasing" to "greasing lubrication",

        // === Connectors, Prepositions & Common Grammar Words ===
        "yang" to "that",
        "dan" to "and",
        "atau" to "or",
        "tetapi" to "however",
        "namun" to "however",
        "karena" to "due to",
        "sebab" to "because of",
        "sehingga" to "resulting in",
        "maka" to "therefore",
        "untuk" to "for",
        "agar" to "in order to",
        "supaya" to "so that",
        "pada" to "on / at",
        "di" to "at / in",
        "ke" to "to",
        "dari" to "from",
        "oleh" to "by",
        "dengan" to "with",
        "tanpa" to "without",
        "tentang" to "regarding",
        "mengenai" to "regarding",
        "antara" to "between",
        "sampai" to "until / up to",
        "hingga" to "up to",
        "sejak" to "since",
        "selama" to "during",
        "saat" to "during",
        "ketika" to "when",
        "waktu" to "time",
        "sebelum" to "before",
        "sesudah" to "after",
        "setelah" to "after",
        "setiap" to "every",
        "semua" to "all",
        "seluruh" to "entire",
        "sebagian" to "partially",
        "beberapa" to "several",
        "banyak" to "multiple",
        "sedikit" to "minor",
        "cukup" to "adequate",
        "lebih" to "more",
        "paling" to "most",
        "sangat" to "very",
        "terlalu" to "excessively",
        "juga" to "also",
        "pun" to "also",
        "selalu" to "always",
        "sering" to "frequently",
        "kadang" to "occasionally",
        "jarang" to "rarely",
        "tidak pernah" to "never",
        "masih" to "still",
        "belum" to "not yet",
        "sudah" to "already",
        "telah" to "has been",
        "sedang" to "currently",
        "akan" to "will be",
        "harus" to "must",
        "wajib" to "must mandatory",
        "dapat" to "can",
        "bisa" to "can / able to",
        "mampu" to "capable of",
        "boleh" to "permitted to",
        "tidak boleh" to "prohibited / do not",
        "jangan" to "do not",
        "hanya" to "only",
        "cuma" to "only",
        "bahkan" to "even",
        "yaitu" to "namely",
        "yakni" to "namely",
        "contoh" to "example",
        "misalnya" to "for example",
        "seperti" to "such as",
        "sebagai" to "as",
        "bahwa" to "that",
        "serta" to "as well as",
        "beserta" to "together with",

        // === Locations, Positions & Orientations ===
        "depan" to "front",
        "belakang" to "rear",
        "kiri" to "left (LH)",
        "kanan" to "right (RH)",
        "atas" to "upper / top",
        "bawah" to "lower / bottom",
        "dalam" to "inner / inside",
        "luar" to "outer / outside",
        "samping" to "side",
        "tengah" to "center / middle",
        "ujung" to "tip / end",
        "pangkal" to "base / root",
        "dekat" to "near",
        "jauh" to "far",
        "sekitar" to "around / vicinity of",
        "area" to "area",
        "posisi" to "position",
        "lokasi" to "location",
        "bagian" to "section / part",
        "sebelah" to "beside / on the side of",
        "sisi" to "side",
        "titik" to "point",
        "workshop" to "workshop",
        "site" to "project site",
        "tambang" to "mine site",
        "lapangan" to "field",
        "gudang" to "warehouse / store",
        "tanjakan" to "incline / uphill slope",
        "turunan" to "decline / downhill slope",

        // === Report Headings, Status & Attributes ===
        "riwayat" to "history",
        "catatan" to "notes",
        "inspeksi" to "inspection",
        "rekomendasi" to "recommendations",
        "kesimpulan" to "conclusion",
        "tindakan" to "action taken",
        "kondisi" to "condition",
        "penyebab" to "root cause",
        "gejala" to "symptoms",
        "keterangan" to "remarks",
        "catatan penting" to "important note",
        "hasil" to "result",
        "status" to "status",
        "aman" to "safe / good",
        "baik" to "good",
        "buruk" to "poor",
        "kritis" to "critical",
        "urgent" to "urgent",
        "darurat" to "emergency",
        "standby" to "standby",
        "breakdown" to "breakdown",
        "selesai" to "completed",
        "tuntas" to "fully resolved",
        "pending" to "pending",
        "menunggu" to "awaiting",
        "sebelumnya" to "previously",
        "selanjutnya" to "subsequently",
        "kemudian" to "then",
        "segera" to "immediately",
        "berkala" to "periodic",
        "terakhir" to "last / latest",
        "berhasil" to "successfully completed",
        "segera diganti" to "requires immediate replacement",
        "perlu diperbaiki" to "needs repair",
        "perlu diganti" to "needs replacement",
        "perlu dimonitor" to "requires routine monitoring",
        "perlu diinspeksi" to "requires inspection",
        "perlu dibersihkan" to "needs cleaning",
        "perlu disetel" to "needs adjustment",
        "sudah selesai" to "has been completed",
        "sedang dikerjakan" to "in progress",
        "belum selesai" to "pending completion"
    )

    /**
     * Translates an Indonesian technical text into a polished, professional English Field Service Report.
     */
    fun translateToEnglish(sourceText: String): String {
        if (sourceText.isBlank()) return ""

        // Check if text is predominantly English
        if (isPureEnglish(sourceText)) {
            return formatEnglishParagraph(sourceText)
        }

        val lines = sourceText.trim().split("\n")
        val translatedLines = lines.map { line ->
            translateSingleLine(line)
        }

        val result = translatedLines.joinToString("\n")
        return formatEnglishParagraph(result)
    }

    private fun translateSingleLine(line: String): String {
        if (line.isBlank()) return ""

        val trimmed = line.trim()
        val bulletPrefix = when {
            trimmed.startsWith("•") -> "• "
            trimmed.startsWith("-") -> "- "
            trimmed.startsWith("*") -> "• "
            trimmed.matches("^\\d+[.)]\\s*.*".toRegex()) -> {
                val match = "^(\\d+[.)]\\s*)".toRegex().find(trimmed)
                match?.value ?: ""
            }
            else -> ""
        }

        val rawContent = if (bulletPrefix.isNotEmpty()) {
            trimmed.substring(bulletPrefix.length).trim()
        } else {
            trimmed
        }

        // Pass 0: Normalize abbreviations / slang (e.g. "yg", "sdh", "krn", "tdk")
        var translated = rawContent
        for ((patternStr, replacement) in slangNormalizationMap) {
            val pattern = Pattern.compile("(?i)" + patternStr)
            translated = pattern.matcher(translated).replaceAll(replacement)
        }

        // Pass 1: Match multi-word technical phrases (highest priority)
        for ((indoPhrase, engPhrase) in phraseMap) {
            val pattern = Pattern.compile("(?i)\\b" + Pattern.quote(indoPhrase) + "\\b")
            translated = pattern.matcher(translated).replaceAll(engPhrase)
        }

        // Pass 2: Replace compound terms and single technical terms (sorted by descending length)
        val sortedKeys = technicalDictionary.keys.sortedByDescending { it.length }
        for (indoTerm in sortedKeys) {
            val engTerm = technicalDictionary[indoTerm] ?: continue
            val pattern = Pattern.compile("(?i)\\b" + Pattern.quote(indoTerm) + "\\b")
            translated = pattern.matcher(translated).replaceAll(engTerm)
        }

        // Pass 3: Suffix '-nya' morphological handling for leftover terms (e.g. "kondisinya" -> "its condition")
        translated = translated.replace("(?i)\\b([a-zA-Z]+)nya\\b".toRegex()) { matchResult ->
            val stem = matchResult.groupValues[1].lowercase(Locale.getDefault())
            technicalDictionary[stem]?.let { "its $it" } ?: matchResult.value
        }

        // Pass 4: Standardize units and hours indicators (e.g. "4500 hm" -> "4,500 HM", "250 jam" -> "250 Hours", "150 bar" -> "150 Bar")
        translated = translated.replace("(?i)\\b(\\d+)\\s*(hm|jam|hours|hour)\\b".toRegex()) { matchResult ->
            val num = matchResult.groupValues[1]
            val unit = matchResult.groupValues[2].lowercase(Locale.getDefault())
            val formattedNum = try {
                String.format(Locale.US, "%,d", num.toLong())
            } catch (_: Exception) {
                num
            }
            if (unit == "hm") "$formattedNum HM" else "$formattedNum Hours"
        }
        translated = translated.replace("(?i)\\b(\\d+)\\s*(bar|psi|rpm|volt|v|kw|ton)\\b".toRegex()) { matchResult ->
            val num = matchResult.groupValues[1]
            val unit = matchResult.groupValues[2].uppercase(Locale.getDefault())
            "$num $unit"
        }

        // Pass 5: Post-processing grammatical cleanup for standard heavy equipment reporting
        translated = postProcessGrammar(translated)

        // Clean extra whitespace
        translated = translated.replace("\\s+".toRegex(), " ").trim()

        // Capitalize first character
        if (translated.isNotEmpty()) {
            translated = translated.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }

        return if (bulletPrefix.isNotEmpty()) "$bulletPrefix$translated" else translated
    }

    /**
     * Cleans up common grammatical awkwardness in translated technical phrases.
     */
    private fun postProcessGrammar(text: String): String {
        var res = text
        // Clean double prepositions
        res = res.replace("(?i)\\bon / at at\\b".toRegex(), "at")
        res = res.replace("(?i)\\bon / at in\\b".toRegex(), "in")
        res = res.replace("(?i)\\bat / in at\\b".toRegex(), "at")
        res = res.replace("(?i)\\bat / in in\\b".toRegex(), "in")
        res = res.replace("(?i)\\bthat that\\b".toRegex(), "that")
        res = res.replace("(?i)\\band and\\b".toRegex(), "and")
        res = res.replace("(?i)\\bis is\\b".toRegex(), "is")
        res = res.replace("(?i)\\bhas been has been\\b".toRegex(), "has been")

        // Smooth standard orderings
        res = res.replace("(?i)\\boil hydraulic\\b".toRegex(), "hydraulic oil")
        res = res.replace("(?i)\\boil engine\\b".toRegex(), "engine oil")
        res = res.replace("(?i)\\boil transmission\\b".toRegex(), "transmission oil")
        res = res.replace("(?i)\\bhose hydraulic\\b".toRegex(), "hydraulic hose")
        res = res.replace("(?i)\\bcylinder boom\\b".toRegex(), "boom cylinder")
        res = res.replace("(?i)\\bcylinder outrigger\\b".toRegex(), "outrigger cylinder")
        res = res.replace("(?i)\\bfilter fuel\\b".toRegex(), "fuel filter")
        res = res.replace("(?i)\\bfilter oil\\b".toRegex(), "oil filter")
        res = res.replace("(?i)\\bfilter air\\b".toRegex(), "air filter")

        return res
    }

    /**
     * Post-processes raw online translation to enforce standard crane & heavy equipment terminology,
     * acronyms (LMI, A2B, PTO, ECU, PM, P2H), and metric unit formatting.
     */
    fun enhanceOnlineTranslation(text: String): String {
        var res = postProcessGrammar(text)

        // Ensure proper acronym capitalization
        val acronyms = listOf("LMI", "A2B", "PTO", "ECU", "CAN", "PM", "P2H", "FSR", "OEM", "LH", "RH", "HM")
        for (acr in acronyms) {
            res = res.replace("(?i)\\b${acr}\\b".toRegex(), acr)
        }

        // Standardize hours & units
        res = res.replace("(?i)\\b(\\d+)\\s*(hm|hours|hour)\\b".toRegex()) { matchResult ->
            val num = matchResult.groupValues[1]
            val unit = matchResult.groupValues[2].lowercase(Locale.getDefault())
            val formattedNum = try {
                String.format(Locale.US, "%,d", num.toLong())
            } catch (_: Exception) {
                num
            }
            if (unit == "hm") "$formattedNum HM" else "$formattedNum Hours"
        }
        res = res.replace("(?i)\\b(\\d+)\\s*(bar|psi|rpm|volt|kw|ton)\\b".toRegex()) { matchResult ->
            val num = matchResult.groupValues[1]
            val unit = matchResult.groupValues[2].uppercase(Locale.getDefault())
            "$num $unit"
        }

        return formatEnglishParagraph(res)
    }

    private fun isPureEnglish(text: String): Boolean {
        val indonesianIndicators = listOf(
            "riwayat", "yang", "dan", "pada", "kebocoran", "mesin", "ditemukan", "telah",
            "dilakukan", "penggantian", "perbaikan", "rembesan", "rusak", "pecah", "kendor",
            "kendur", "aus", "selesai", "sudah", "catatan", "sebelumnya", "tindakan", "kesimpulan",
            "oli", "baut", "selang", "kabel", "panas", "kurang", "ganti", "cek", "periksa",
            "dibersihkan", "disetel", "dikencangkan", "siap", "operasi", "bocor", "patah", "retak"
        )

        val words = text.lowercase(Locale.getDefault()).split("\\W+".toRegex()).filter { it.isNotBlank() }
        if (words.isEmpty()) return true

        val indoMatches = words.count { it in indonesianIndicators }
        // If there are any recognizable Indonesian technical words, translate it!
        return indoMatches == 0
    }

    private fun formatEnglishParagraph(text: String): String {
        return text
            .replace("\\s+,", ",")
            .replace("\\s+\\.", ".")
            .replace("\\s+;", ";")
            .replace(" ,", ",")
            .replace(" .", ".")
            .trim()
    }
}
