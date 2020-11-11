package club.cannoner.crystalmoduleapi;

import lombok.Getter;

import java.util.HashMap;

public class Config {
    @Getter private boolean patchcrumbs;
    @Getter private boolean breadcrumbs;
    @Getter private boolean adjustHelper;
    @Getter private boolean floatFinder;
    @Getter private boolean dispenserCheck;

    public Config(HashMap<String, Boolean> entries) {
        patchcrumbs = entries.get("patchcrumbs");
        breadcrumbs = entries.get("breadcrumbs");
        adjustHelper = entries.get("adjusthelper");
        floatFinder = entries.get("floatfinder");
        dispenserCheck = entries.get("dispensercheck");
    }

    @Override
    public String toString() {
        return "Patchcrumbs: " + (patchcrumbs ? "disabled" : "enabled")
                + "\nBreadcrumbs: " + (breadcrumbs ? "disabled" : "enabled")
                + "\nAdjust Helper: " + (adjustHelper ? "disabled" : "enabled")
                + "\nFloat Finder: " + (floatFinder ? "disabled" : "enabled")
                + "\nDispenser Check: " + (dispenserCheck ? "disabled" : "enabled");
    }
}
