package daybreak.abilitywar.game.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import daybreak.abilitywar.game.games.defaultgame.Game;

/**
 * {@link Game} 관련 이벤트
 */
public abstract class GameEvent extends Event {

	private final Game game;
	
	protected GameEvent(Game game) {
		this.game = game;
	}

	public Game getGame() {
		return game;
	}

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
