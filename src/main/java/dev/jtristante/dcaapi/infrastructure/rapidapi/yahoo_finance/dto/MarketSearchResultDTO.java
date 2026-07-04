package dev.jtristante.dcaapi.infrastructure.rapidapi.yahoo_finance.dto;

public record MarketSearchResultDTO(
        String shortname,
        String quoteType,
        String symbol,
        String index,
        Double score,
        String typeDisp,
        String longname,
        String exchDisp,
        String sector,
        String sectorDisp,
        String industry,
        String industryDisp
) {

    public static Builder builder() {
        return new Builder();
    }

    public static MarketSearchResultDTO build(String symbol, String shortname, String quoteType) {
        return new Builder()
                .symbol(symbol)
                .shortname(shortname)
                .quoteType(quoteType)
                .build();
    }

    public static MarketSearchResultDTO crypto(String symbol, String name) {
        return new Builder()
                .symbol(symbol)
                .shortname(name)
                .quoteType("CRYPTOCURRENCY")
                .build();
    }

    public static MarketSearchResultDTO stock(String symbol, String name) {
        return new Builder()
                .symbol(symbol)
                .shortname(name)
                .quoteType("EQUITY")
                .build();
    }

    public static MarketSearchResultDTO etf(String symbol, String name) {
        return new Builder()
                .symbol(symbol)
                .shortname(name)
                .quoteType("ETF")
                .build();
    }

    public static class Builder {
        private String shortname;
        private String quoteType;
        private String symbol;
        private String index;
        private Double score;
        private String typeDisp;
        private String longname;
        private String exchDisp;
        private String sector;
        private String sectorDisp;
        private String industry;
        private String industryDisp;

        public Builder shortname(String shortname) {
            this.shortname = shortname;
            return this;
        }

        public Builder quoteType(String quoteType) {
            this.quoteType = quoteType;
            return this;
        }

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder index(String index) {
            this.index = index;
            return this;
        }

        public Builder score(Double score) {
            this.score = score;
            return this;
        }

        public Builder typeDisp(String typeDisp) {
            this.typeDisp = typeDisp;
            return this;
        }

        public Builder longname(String longname) {
            this.longname = longname;
            return this;
        }

        public Builder exchDisp(String exchDisp) {
            this.exchDisp = exchDisp;
            return this;
        }

        public Builder sector(String sector) {
            this.sector = sector;
            return this;
        }

        public Builder sectorDisp(String sectorDisp) {
            this.sectorDisp = sectorDisp;
            return this;
        }

        public Builder industry(String industry) {
            this.industry = industry;
            return this;
        }

        public Builder industryDisp(String industryDisp) {
            this.industryDisp = industryDisp;
            return this;
        }

        public MarketSearchResultDTO build() {
            return new MarketSearchResultDTO(
                    shortname,
                    quoteType,
                    symbol,
                    index,
                    score,
                    typeDisp,
                    longname,
                    exchDisp,
                    sector,
                    sectorDisp,
                    industry,
                    industryDisp
            );
        }
    }
}
