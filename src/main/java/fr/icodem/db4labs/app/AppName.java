package fr.icodem.db4labs.app;

public enum AppName {
    Common,
    EShop,
    EShopMedia,
    EShopGrocery,
    BookATable;
    // TODO remove common + remove db name and app name from common-schema.json

    public static class AppConfig {
        private String title;
        private String dataFilename;
        private String schema;

        public AppConfig(String title, String dataFilename, String schema) {
            this.title = title;
            this.dataFilename = dataFilename;
            this.schema = schema;
        }

        public String getTitle() {
            return title;
        }

        public String getDataFilename() {
            return dataFilename;
        }

        public String getSchema() {
            return schema;
        }
    }

    /*
    public String getTitle() {
        switch (this) {
            case EShop:
                return "EShop";
            case EShopMedia:
                return "EShop Media";
            case EShopGrocery:
                return "EShop Grocery";
        }
        return null;
    }  */

    public AppConfig getConfig() {
        AppConfig cfg = null;
        switch (this) {
            case  EShop:
                //cfg = new AppConfig("EShop", "data-eshop.zip",
                //        "common-schema.json", "eshop-schema.json");
                cfg = new AppConfig("EShop", "data-eshop.zip", "eshop-schema.json");
                break;
            case  EShopMedia:
                //cfg = new AppConfig("EShop Media", "data-eshop-media.zip",
                //        "common-schema.json", "eshop-schema.json", "eshop-media-schema.json");
                cfg = new AppConfig("EShop Media", "data-eshop-media.zip", "eshop-media-schema.json");
                break;
            case EShopGrocery:
                //cfg = new AppConfig("EShop Grocery", "data-eshop-grocery.zip",
                //        "common-schema.json", "eshop-schema.json", "eshop-grocery-schema.json");
                cfg = new AppConfig("EShop Grocery", "data-eshop-grocery.zip", "eshop-grocery-schema.json");
                break;
            case BookATable:
                cfg = new AppConfig("Book a Table", "data-book-a-table.zip", "book-a-table-schema.json");
                break;
        }

        return cfg;
    }

}
