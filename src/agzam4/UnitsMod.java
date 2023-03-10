package agzam4;

import arc.Core;
import arc.Events;
import arc.KeyBinds.Axis;
import arc.KeyBinds.KeyBind;
import arc.KeyBinds.Section;
import arc.func.Boolc;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.input.InputDevice.DeviceType;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Table;
import arc.scene.utils.Selection;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.game.EventType.Trigger;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders;
import mindustry.mod.Mod;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.SettingsMenuDialog;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsCategory;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable;
import mindustry.world.Block;

import static mindustry.Vars.*;
import static mindustry.content.UnitTypes.*;

import java.util.Arrays;
import java.util.Locale;

public class UnitsMod extends Mod {

	private static boolean hideUnits, comfortMega, drawUnitsHitboxes;
	private static Axis hideUnitsHotkey = new Axis(KeyCode.h);

	private static final String settingCategoryName = Locale.getDefault() == Locale.ENGLISH ? "Units settings" : "Настройки юнитов";
	private static final String buttonsText[] = Locale.getDefault() == Locale.ENGLISH ? 
			new String[] {"Hide units body", "Hide units key", "No \"ice moving\" on poly, mega, and", "Show hitboxes on hide"}: 
			new String[] {"Скрывать корпус юнитов", "Клаиша скрытия юнитов", "Убрать скольжение у меги", "Отоброжать хитбоксы когда скрыты"};
	
	private UnitTextures[] unitTextures;
	
	private TextureRegion none;
	private TextureRegion minelaser, minelaserEnd;

	private UnitType[] units;

	@Override
	public void init() {
		
		try {
			Object hotkeyObject = Core.settings.get("agzam4mod-units.settings.hideUnitsHotkey", null);
			if(hotkeyObject == null) {
				hideUnitsHotkey = new Axis(KeyCode.h);
			} else {
				if(hotkeyObject instanceof String) {
	                KeyCode keyCode = Arrays.stream(KeyCode.values()).filter(k -> k.value.equalsIgnoreCase((String) hotkeyObject)).findFirst().orElse(KeyCode.h);
					hideUnitsHotkey = new Axis(keyCode);
				} else if(hotkeyObject instanceof Integer) {
					hideUnitsHotkey = new Axis(KeyCode.byOrdinal((Integer) hotkeyObject));
				} else {
					hideUnitsHotkey = new Axis(KeyCode.h);
				}
			}
			hideUnits = Core.settings.getBool("agzam4mod-units.settings.hideUnits", false);
			comfortMega = Core.settings.getBool("agzam4mod-units.settings.comfortMega", false);
			drawUnitsHitboxes = Core.settings.getBool("agzam4mod-units.settings.drawUnitsHitboxes", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		none = new TextureRegion(Core.atlas.find("agzam4mod-units-none"));
		minelaser = Core.atlas.find("minelaser");
		minelaserEnd = Core.atlas.find("minelaser-end");
		
		units = new UnitType[] {
				mace, dagger, crawler, fortress, scepter, reign, vela,
				nova, pulsar, quasar,
				corvus, atrax,
			    merui, cleroi, anthicus,
			    tecta, collaris,
			    spiroct, arkyid, toxopid,
			    elude,
			    flare, eclipse, horizon, zenith, antumbra,
			    mono, poly, mega,
			    quell, disrupt,
			    quad, oct,
			    risso, minke, bryde, sei, omura, retusa, oxynoe, cyerce, aegires, navanax,
			    stell, locus, precept, vanquish, conquer
		};
		
		unitTextures = new UnitTextures[units.length];
		for (int i = 0; i < units.length; i++) {
			unitTextures[i] = new UnitTextures(units[i]);
		}

		//		UnitTypes.alpha.baseRegion.

		Cons<SettingsMenuDialog.SettingsTable> builder = settingsTable -> {
				
			// Core.settings.put("agzam4mod-units.settings.hideUnitsHotkey"
            Table hotkeyTable = new Table();
            hotkeyTable.add().height(10);
            hotkeyTable.row();
            hotkeyTable.add(buttonsText[1], Color.white).left().padRight(40).padLeft(8);
            hotkeyTable.label(() -> hideUnitsHotkey.key.toString()).color(Pal.accent).left().minWidth(90).padRight(20);
            hotkeyTable.button("@settings.rebind", Styles.defaultt, () -> {
            	openDialog();
            }).width(130f);
            hotkeyTable.button("@settings.resetKey", Styles.defaultt, () -> {
            	hideUnitsHotkey = new Axis(KeyCode.h);
                Core.settings.remove("agzam4mod-units.settings.hideUnits");
        		Core.settings.saveValues();
            }).width(130f).pad(2f).padLeft(4f);
            hotkeyTable.row();
            settingsTable.add(hotkeyTable);
            settingsTable.row();

			SettingsTable table = new SettingsTable();
			table.row();
			table.checkPref(buttonsText[0], hideUnits, new Boolc() {
				
				@Override
				public void get(boolean b) {
					hideUnits(b);
				}
			});
			table.row();

			table.checkPref(buttonsText[2], hideUnits, new Boolc() {
				
				@Override
				public void get(boolean b) {
					comfortMega(b);
				}
			});
			table.row();

			table.checkPref(buttonsText[3], hideUnits, new Boolc() {
				
				@Override
				public void get(boolean b) {
					drawUnitsHitboxes(b);
				}
			});
			table.row();
            settingsTable.add(table);

            settingsTable.row();
            // Core.bundle.get("keybind." + hideUnitsHotkey.key.name() + ".name"
            

			settingsTable.name = settingCategoryName;
			settingsTable.visible = true;
		};
		
		ui.settings.getCategories().add(new SettingsCategory(settingCategoryName, new TextureRegionDrawable(UnitTypes.poly.region), builder));

		Core.scene.addListener(new InputListener() {
	            @Override
	            public boolean keyDown(InputEvent event, KeyCode keyCode) {
	                if (!state.isMenu() &&
	                        !ui.chatfrag.shown() &&
	                        !ui.schematics.isShown() &&
	                        !ui.database.isShown() &&
	                        !ui.consolefrag.shown() &&
	                        !ui.content.isShown()) {
	                	if(hideUnitsHotkey != null) {
		                    if (keyCode.equals(UnitsMod.hideUnitsHotkey.key)) {
		                    	hideUnits(!hideUnits);
		                    }
	                	}
	                }

	                return super.keyDown(event, keyCode);
	            }
	        });
		
		megaAccel = mega.accel;
		megaDragg = mega.drag;
		megaSpeed = mega.speed;
		 
		Events.run(Trigger.draw, () -> {
			if(drawUnitsHitboxes && hideUnits) {
				for (int i = 0; i < Groups.unit.size(); i++) {
					Unit unit = Groups.unit.index(i);
					UnitType unitType = unit.type;
					Team team = unit.team();
					
					if(unitType == alpha || unitType == beta || unitType == gamma) {
						if(team == player.team()) continue;
					}
					
	                Draw.reset();
	                Draw.z(Layer.buildBeam);

	                Draw.color(team.color, Color.black, .25f);
	                Fill.square(unit.x, unit.y, unit.hitSize/2f + 1, 45);
	                
	                Draw.color(team.color);
	                Fill.square(unit.x, unit.y, unit.hitSize/2f, 45);
	                
	                Draw.reset();
				}
			}
		});
		super.init();
	}

	protected void drawUnitsHitboxes(boolean b) {
		drawUnitsHitboxes = b;		
		Core.settings.put("agzam4mod-units.settings.drawUnitsHitboxes", b);
		Core.settings.saveValues();
	}

	private float megaAccel, megaDragg, megaSpeed;

	private void comfortMega(boolean b) {
		comfortMega = b;
		Core.settings.put("agzam4mod-units.settings.comfortMega", b);
		Core.settings.saveValues();
		if(comfortMega) {
			mega.accel = emanate.accel;
			mega.drag = emanate.drag;
//			mega.speed = 3;
		} else {
			mega.accel = megaAccel;
			mega.drag = megaDragg;
//			mega.speed = megaSpeed;
		}
	}
	
	private void hideUnits(boolean b) {
		hideUnits = b;
		Core.settings.put("agzam4mod-units.settings.hideUnits", b);
		Core.settings.saveValues();
		if(b) {
			for (int i = 0; i < unitTextures.length; i++) {
				unitTextures[i].hideTextures();
				unitTextures[i].hideEngines();
			}

			Core.atlas.addRegion("minelaser", none);
			Core.atlas.addRegion("minelaser-end", none);
		} else {
			for (int i = 0; i < units.length; i++) {
				unitTextures[i].returnTextures();
				unitTextures[i].returnEngines();
			}
			Core.atlas.addRegion("minelaser", minelaser);
			Core.atlas.addRegion("minelaser-end", minelaserEnd);
		}
	}

	Section section = Core.keybinds.getSections()[0];
    
	private void openDialog() {
		Dialog keybindDialog = new Dialog(Core.bundle.get("keybind.press"));

		keybindDialog.titleTable.getCells().first().pad(4);
			
        if(section.device.type() == DeviceType.keyboard){

        	keybindDialog.addListener(new InputListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                    if(Core.app.isAndroid()) return false;
                    rebind(keybindDialog, section, button);
                    return false;
                }

                @Override
                public boolean keyDown(InputEvent event, KeyCode button){
                	keybindDialog.hide();
                    if(button == KeyCode.escape) return false;
                    rebind(keybindDialog, section, button);
                    return false;
                }

                @Override
                public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                    keybindDialog.hide();
                    rebind(keybindDialog, section, KeyCode.scroll);
                    return false;
                }
            });
        }

        keybindDialog.show();
        Time.runTask(1f, () -> keybindDialog.getScene().setScrollFocus(keybindDialog));
    }
	
	void rebind(Dialog rebindDialog, Section section, KeyCode newKey){
        rebindDialog.hide();
        hideUnitsHotkey = new Axis(newKey);
        Core.settings.put("agzam4mod-units.settings.hideUnitsHotkey", newKey.value);
		Core.settings.saveValues();
    }
	//  Core.settings.put("agzam4mod-units.settings.hideUnitsHotkey", new java.lang.Integer(75))
	// Core.settings.getInt("agzam4mod-units.settings.hideUnitsHotkey", KeyCode.h.ordinal())
}
