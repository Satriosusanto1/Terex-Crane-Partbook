package com.example.data.model

data class UnitCatalogItem(
    val unitNo: String,
    val model: String,
    val serialNo: String,
    val brand: String = "LIEBHERR"
)

object UnitDatabase {
    val catalog: List<UnitCatalogItem> = listOf(
        UnitCatalogItem(unitNo = "MC041", model = "LTM1120-4.1", serialNo = "81648", brand = "LIEBHERR"),
        UnitCatalogItem(unitNo = "MC042", model = "LTM1120-4.1", serialNo = "81651", brand = "LIEBHERR"),
        UnitCatalogItem(unitNo = "MC043", model = "LTM1160-5.2", serialNo = "46593", brand = "LIEBHERR"),
        UnitCatalogItem(unitNo = "MC044", model = "LTM1160-5.2", serialNo = "46609", brand = "LIEBHERR"),
        UnitCatalogItem(unitNo = "MC045", model = "LTM1250-5.1", serialNo = "44337", brand = "LIEBHERR"),
        UnitCatalogItem(unitNo = "MC046", model = "LTM1250-5.1", serialNo = "44346", brand = "LIEBHERR"),
        UnitCatalogItem(unitNo = "MC047", model = "LTM1250-5.1", serialNo = "44355", brand = "LIEBHERR"),
        UnitCatalogItem(unitNo = "MC048", model = "LR1800-1.0", serialNo = "48436", brand = "LIEBHERR"),
        UnitCatalogItem(unitNo = "MC049", model = "LR1800-1.0", serialNo = "48438", brand = "LIEBHERR"),
        UnitCatalogItem(unitNo = "MC018", model = "AC40", serialNo = "251761", brand = "DEMAG / TEREX"),
        UnitCatalogItem(unitNo = "MC019", model = "AC80", serialNo = "48232", brand = "DEMAG / TEREX"),
        UnitCatalogItem(unitNo = "MC020", model = "AC100", serialNo = "10213", brand = "DEMAG / TEREX"),
        UnitCatalogItem(unitNo = "MC021", model = "AC140", serialNo = "76625", brand = "DEMAG / TEREX"),
        UnitCatalogItem(unitNo = "MC022", model = "AC200", serialNo = "33340", brand = "DEMAG / TEREX"),
        UnitCatalogItem(unitNo = "MC023", model = "AC250", serialNo = "85198", brand = "DEMAG / TEREX"),
        UnitCatalogItem(unitNo = "MC024", model = "AC350", serialNo = "32088", brand = "DEMAG / TEREX"),
        UnitCatalogItem(unitNo = "MC035", model = "Terex HC275", serialNo = "OKEA0324", brand = "TEREX"),
        UnitCatalogItem(unitNo = "MC036", model = "MAC25", serialNo = "251399", brand = "FRANNA / TEREX"),
        UnitCatalogItem(unitNo = "MC037", model = "AT15-3", serialNo = "AT152177", brand = "FRANNA / TEREX"),
        UnitCatalogItem(unitNo = "MC038", model = "MAC25", serialNo = "251731", brand = "FRANNA / TEREX"),
        UnitCatalogItem(unitNo = "MC039", model = "MAC25", serialNo = "251765", brand = "FRANNA / TEREX"),
        UnitCatalogItem(unitNo = "MC040", model = "MAC25", serialNo = "251764", brand = "FRANNA / TEREX"),
        UnitCatalogItem(unitNo = "MC050", model = "MAC25", serialNo = "251794", brand = "FRANNA / TEREX"),
        UnitCatalogItem(unitNo = "EX026", model = "RHL380D", serialNo = "19", brand = "FUCHS / TEREX")
    )

    fun findByUnitNo(unitNo: String): UnitCatalogItem? {
        val clean = unitNo.trim().replace("-", "").replace(" ", "")
        return catalog.firstOrNull {
            it.unitNo.replace("-", "").replace(" ", "").equals(clean, ignoreCase = true)
        }
    }

    fun searchMatching(query: String): List<UnitCatalogItem> {
        if (query.isBlank()) return catalog
        val clean = query.trim().replace("-", "").replace(" ", "")
        return catalog.filter {
            it.unitNo.replace("-", "").replace(" ", "").contains(clean, ignoreCase = true) ||
            it.model.contains(query.trim(), ignoreCase = true) ||
            it.serialNo.contains(query.trim(), ignoreCase = true)
        }
    }
}
