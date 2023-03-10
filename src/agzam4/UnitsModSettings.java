package agzam4;

import mindustry.graphics.Pal;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting;

import java.util.Locale;

import static mindustry.Vars.*;

public class UnitsModSettings extends Setting {
	
	public UnitsModSettings() {
		super("hm");
	}

	@Override
	public void add(SettingsTable table) {
        table.labelWrap("Test").fillX().get().setWrap(true);
        table.image().growX().pad(10, 0, 10, 0).color(Pal.gray);
        table.row();
	}

}
