import Exception.cfg.ConfigException;
import appFlow.AppSetup;
import cfg.Config;
import model.Section;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        try { Config.init("cfg.toml"); }
        catch (ConfigException e) { e.printStackTrace(); }

        AppSetup setup = new AppSetup();
        AppSetup.AppData appData = setup.setup();


    }
}
