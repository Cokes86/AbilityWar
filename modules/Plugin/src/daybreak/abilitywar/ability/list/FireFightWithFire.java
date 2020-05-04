package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import java.util.Random;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

@AbilityManifest(name = "이열치열", rank = Rank.B, species = Species.HUMAN, explain = {
		"§c화염 §f대미지를 받을 때, $[ChanceConfig]% 확률로 대미지만큼 체력을 회복합니다."
})
public class FireFightWithFire extends AbilityBase {

	public static final SettingObject<Integer> ChanceConfig = abilitySettings.new SettingObject<Integer>(FireFightWithFire.class, "Chance", 50,
			"# 공격을 받았을 시 몇 퍼센트 확률로 회복을 할지 설정합니다.",
			"# 50은 50%를 의미합니다.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1 && value <= 100;
		}

	};

	public FireFightWithFire(Participant participant) {
		super(participant);
	}

	private final int chance = ChanceConfig.getValue();
	private final Random random = new Random();

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			if (e.getCause().equals(DamageCause.FIRE) || e.getCause().equals(DamageCause.FIRE_TICK)) {
				if (random.nextInt(100) <= chance - 1) {
					double damage = e.getDamage();
					e.setDamage(0);

					if (!getPlayer().isDead()) {
						getPlayer().setHealth(Math.min(getPlayer().getHealth() + damage, getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		if (e.getEntity().equals(getPlayer())) {
			if (e.getCause().equals(DamageCause.LAVA)) {
				if (random.nextInt(100) <= ChanceConfig.getValue() - 1) {
					double damage = e.getDamage();
					e.setDamage(0);

					if (!getPlayer().isDead()) {
						getPlayer().setHealth(Math.min(getPlayer().getHealth() + damage, getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
					}
				}
			}
		}
	}

}
