package daybreak.abilitywar.game.event;

import daybreak.abilitywar.game.Game;

/**
 * {@link Game}이 종료될 때 호출되는 이벤트입니다.
 */
public class GameEndEvent extends GameEvent {

	public GameEndEvent(Game game) {
		super(game);
	}

}
