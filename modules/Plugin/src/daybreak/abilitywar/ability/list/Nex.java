package daybreak.abilitywar.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks;
import daybreak.abilitywar.utils.base.minecraft.FallingBlocks.Behavior;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

@AbilityManifest(name = "넥스", rank = Rank.A, species = Species.GOD, explain = {
		"철괴를 우클릭하면 공중으로 올라갔다 바라보는 방향으로 날아가",
		"내려 찍으며 주변의 플레이어들에게 대미지를 입히고 날려보냅니다. $[CooldownConfig]"
})
public class Nex extends AbilityBase implements ActiveHandler {

	public static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(Nex.class, "Cooldown", 120, "# 쿨타임") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}

	};

	public static final SettingObject<Integer> DamageConfig = abilitySettings.new SettingObject<Integer>(Nex.class, "Damage", 20, "# 대미지") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};

	public Nex(Participant participant) {
		super(participant);
	}

	private final CooldownTimer cooldownTimer = new CooldownTimer(CooldownConfig.getValue());

	private final Timer fallBlockTimer = new Timer(5) {

		Location center;

		@Override
		public void onStart() {
			this.center = getPlayer().getLocation();
		}

		@SuppressWarnings("deprecation")
		@Override
		public void run(int count) {
			int distance = 6 - count;

			if (ServerVersion.getVersionNumber() >= 13) {
				for (Block block : LocationUtil.getBlocks2D(center, distance, true, true, true)) {
					if (block.getType() == Material.AIR) block = block.getRelative(BlockFace.DOWN);
					if (block.getType() == Material.AIR) continue;
					Location location = block.getLocation().add(0, 1, 0);
					FallingBlocks.spawnFallingBlock(location, block.getType(), false, getPlayer().getLocation().toVector().subtract(location.toVector()).multiply(-0.1).setY(Math.random()), Behavior.FALSE);
				}
			} else {
				for (Block block : LocationUtil.getBlocks2D(center, distance, true, true, true)) {
					if (block.getType() == Material.AIR) block = block.getRelative(BlockFace.DOWN);
					if (block.getType() == Material.AIR) continue;
					Location location = block.getLocation().add(0, 1, 0);
					FallingBlocks.spawnFallingBlock(location, block.getType(), block.getData(), false, getPlayer().getLocation().toVector().subtract(location.toVector()).multiply(-0.1).setY(Math.random()), Behavior.FALSE);
				}
			}

			for (Damageable damageable : LocationUtil.getNearbyDamageableEntities(center, 5, 5)) {
				if (!damageable.equals(getPlayer())) {
					damageable.setVelocity(center.toVector().subtract(damageable.getLocation().toVector()).multiply(-1).setY(1.2));
				}
			}
		}

	}.setPeriod(TimeUnit.TICKS, 4);
	private boolean noFallDamage = false;
	private boolean skillEnabled = false;
	private final Timer skill = new Timer(4) {

		@Override
		public void onStart() {
			noFallDamage = true;
			getPlayer().setVelocity(getPlayer().getVelocity().add(new Vector(0, 4, 0)));
		}

		@Override
		public void run(int count) {
		}

		@Override
		public void onEnd() {
			skillEnabled = true;
			Vector playerDirection = getPlayer().getLocation().getDirection();
			getPlayer().setVelocity(getPlayer().getVelocity().add(playerDirection.normalize().multiply(8).setY(-4)));
		}

	}.setPeriod(TimeUnit.TICKS, 10);

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getEntity().equals(getPlayer())) {
				if (noFallDamage) {
					if (e.getCause().equals(DamageCause.FALL)) {
						e.setCancelled(true);
						noFallDamage = false;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getPlayer().equals(getPlayer())) {
			if (skillEnabled) {
				Block b = getPlayer().getLocation().getBlock();
				Block db = getPlayer().getLocation().subtract(0, 1, 0).getBlock();

				if (!b.getType().equals(Material.AIR) || !db.getType().equals(Material.AIR)) {
					skillEnabled = false;
					final double damage = DamageConfig.getValue();
					for (Damageable d : LocationUtil.getNearbyEntities(Damageable.class, getPlayer(), 5, 5)) {
						if (d instanceof Player) SoundLib.ENTITY_GENERIC_EXPLODE.playSound((Player) d);
						d.damage(damage, getPlayer());
					}
					SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer());

					fallBlockTimer.start();
				}
			}
		}
	}

	@Override
	public boolean ActiveSkill(Material materialType, ClickType clickType) {
		if (materialType.equals(Material.IRON_INGOT)) {
			if (clickType.equals(ClickType.RIGHT_CLICK)) {
				if (!cooldownTimer.isCooldown()) {
					for (Player player : LocationUtil.getNearbyPlayers(getPlayer(), 5, 5)) {
						SoundLib.ENTITY_WITHER_SPAWN.playSound(player);
					}
					SoundLib.ENTITY_WITHER_SPAWN.playSound(getPlayer());
					skill.start();
					cooldownTimer.start();
					return true;
				}
			}
		}

		return false;
	}

}
