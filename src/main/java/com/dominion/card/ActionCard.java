package com.dominion.card;

import com.dominion.Turn;

public interface ActionCard extends Card {
	public void playCard(Turn turn);
}
