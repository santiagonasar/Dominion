package com.dominion.card;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.dominion.ClientTurn;
import com.dominion.DominionGUI;
import com.dominion.ServerTurn;
import com.dominion.Turn;
import com.dominion.DominionGUI.SelectionType;

public interface Card extends Serializable, Comparable<Card> {
	public static final TreasureCard[] treasureCards = {new Copper(), new Silver(), new Gold()};
	public static final VictoryCard[] victoryCards = {new Estate(), new Duchy(), new Province()};
	public static final Card curse = new Curse();

	public static final Card[] mustUse = { 

	};

	public static final Card[] baseRandomizerDeck = {
		new Chapel(), new Cellar(), new Moat(), 
		new Chancellor(), new Village(), new Woodcutter(), new Workshop(),
		new Bureaucrat(), new Feast(), new Gardens(), new Militia(), 
		new Moneylender(), new Remodel(), new Smithy(), new Spy(),
		new CouncilRoom(), new Festival(), new Laboratory(), new Library(),
		new Market(), new Mine(), new Witch(),
		new Adventurer()
	};
	public static final Card[] intrigueRandomizerDeck = {
		new Courtyard(), new GreatHall(), new ShantyTown(), new Steward(),
		new Swindler(),
		new Baron(), new Conspirator(), new Coppersmith(), new Ironworks(),
		new MiningVillage(), new SeaHag(),
		new Duke(), new Minion(), new Tribute(), new Upgrade(),
		new Harem()
	};
	public static final Card[] seasideRandomizerDeck= {
		new Bazaar()
	};
//	public static final Card[] alchemyRandomizerDeck= {
//	};
	
	public static final Card[] startingHand = new Card[10];

	public int getCost();

	@SuppressWarnings("serial")
	public abstract static class DefaultCard implements Card {
		@Override public String toString() { return this.getClass().getSimpleName(); }
		@Override public boolean equals(Object other) { return (other.getClass() == this.getClass()); }
		@Override
		public int compareTo(Card other) {
			if(getCost() == other.getCost()) {
				return toString().compareTo(other.toString());
			}
			return getCost() - other.getCost();
		}
	}

	@SuppressWarnings("serial")
	public abstract static class SwitchByType extends DefaultCard {
		// used by both Tribute and Ironworks when you get bonuses based on type
		public void switchHelper(Turn turn, Decision decision, int numCards, int numGain) {
			System.out.println("doing continueProcessing for a tribute");
			List<Card> list = ((Decision.CardListDecision) decision).list;
			if(list.size()!=numCards) return;//TODO do something smarter here?  not sure what
			for(int i = 0; i < numCards; i++) {
				Card c = list.get(i);
				//if both the same, only do it once, only applicable for Tribute
				if(i == 1 && c.equals(list.get(0))) break;
				//note, not else if -- if multiple it gets all of the bonuses!
				if(c instanceof ActionCard) turn.addActions(numGain);
				if(c instanceof VictoryCard) turn.drawCards(numGain);
				if(c instanceof TreasureCard) turn.addBuyingPower(numGain);
				//curses get nothing, as do nulls (i.e. if no cards were left in deck)
			}
		}
	}

	@SuppressWarnings("serial")
	public abstract static class NameAndPronounCard extends DefaultCard {
		// used by attack cards with decisions for all other players
		public String getPlayerName(DominionGUI gui, Decision decision) {
			Decision.DecisionAndPlayerDecision dapd = (Decision.DecisionAndPlayerDecision) decision;
			String name = gui.getPlayerName(dapd.playerNum);
			if(gui.getLocalPlayer() == dapd.playerNum) {
				name = "You";
			}
			return name;
		}
		// TODO: someday we may actually have a way of looking up what pronoun
		// to use if it's another player
		public String getPronoun(DominionGUI gui, Decision decision) {
			Decision.DecisionAndPlayerDecision dapd = (Decision.DecisionAndPlayerDecision) decision;
			String pronoun = "his/her";
			if(gui.getLocalPlayer() == dapd.playerNum) {
				pronoun = "your";
			}
			return pronoun;
		}
	}

	@SuppressWarnings("serial")
	public abstract static class VictorySelectionCard extends DefaultCard implements SelectionCard {
		@Override public boolean isSelectable(Card c) { return c instanceof VictoryCard; }
	}

	@SuppressWarnings("serial")
	public abstract static class TreasureSelectionCard extends DefaultCard implements SelectionCard {
		@Override public boolean isSelectable(Card c) { return c instanceof TreasureCard; }
	}

	public static class Copper extends DefaultCard implements TreasureCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 0; }
		@Override public int getValue() { return 1; }
	}
	public static class Silver extends DefaultCard implements TreasureCard{
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 3; }
		@Override public int getValue() { return 2; }
	}
	public static class Gold extends DefaultCard implements TreasureCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 6; }
		@Override public int getValue() { return 3; }
	}

	public static class Estate extends DefaultCard implements VictoryCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 2; }
		@Override public int getVictoryPoints() { return 1; }
	}

	public static class Duchy extends DefaultCard implements VictoryCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 5; }
		@Override public int getVictoryPoints() { return 3; }
	}
	public static class Province extends DefaultCard implements VictoryCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 8; }
		@Override public int getVictoryPoints() { return 6; }
	}

	//TODO maybe should implement its own "CurseCard" type?
	public static class Curse extends DefaultCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 0; }
		public int getVictoryPoints() { return -1; }
	}

	//Base set 
	
	//twos
	public class Chapel extends DefaultCard implements DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public void playCard(Turn turn) { 
			if(turn instanceof ServerTurn) {
				Decision.CardListDecision decision;
				do {
					//request decision
					decision = (Decision.CardListDecision) ((ServerTurn)turn).getDecision(this, null);
					//try using this decision, if it doesn't work, ask again
				} while(decision.list.size() > 4 || !((ServerTurn)turn).trashCardsFromHand(decision, this));
			}
			//on the client side, just wait for the decision request
		}
		@Override public int getCost() { return 2; }

		@Override
		public void createAndSendDecisionObject(DominionGUI gui, Decision decision) {
			//sets the GUI in motion
			gui.setupCardSelection(4, false, SelectionType.trash, null);
		}
		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum, Decision decision, ClientTurn turn) {
			gui.trashCardSelection(playerNum, (Decision.CardListDecision)decision);
		}
	}	

	public class Cellar extends DefaultCard implements DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public void playCard(Turn turn) { 
			turn.addActions(1);
			if(turn instanceof ServerTurn) {
				ServerTurn st = (ServerTurn)turn;
				Decision.CardListDecision decision;
				// request cards to discard and attempt to discard them til you get a list that's valid
				while(!st.discardCardsFromHand(decision = (Decision.CardListDecision) st.getDecision(this, null), this));
				// now draw as many cards as you discarded
				st.drawCards(decision.list.size());
			}
			// on the client side, just wait for the decision request
		}
		@Override public int getCost() { return 2; }
		@Override
		public void createAndSendDecisionObject(DominionGUI gui, Decision decision) {
			// sets the GUI in motion
			gui.setupCardSelection(-1, false, SelectionType.discard, null);
		}
		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum, Decision decision, ClientTurn turn) {
			// TODO: should this be discard, not trash? look into this.
			gui.trashCardSelection(playerNum, (Decision.CardListDecision)decision);
		}
	}	

	public class Moat extends DefaultCard implements ReactionCard {
		private static final long serialVersionUID = 1L;
		@Override public boolean reaction(Turn turn) { return false; }
		@Override public void playCard(Turn turn) { turn.drawCards(2); }
		@Override public int getCost() { return 2; }
	}	
	
	//threes
	public class Chancellor extends DefaultCard implements DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 3; }

		// TODO: give error if not the right kind of decision?
		@SuppressWarnings("unchecked")
		@Override
		public void playCard(Turn turn) {
			turn.addBuyingPower(2);
			if(turn instanceof ServerTurn) {
				Decision d = ((ServerTurn) turn).getDecision(this, null);
				if(((Decision.EnumDecision<Decision.yesNo>)d).enumValue == Decision.yesNo.yes)
					((ServerTurn) turn).discardDeck();
			}
		}

		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum,
				Decision decision, ClientTurn turn) {
			// Note: nothing to do here unless we add visuals for size of deck/discard
		}

		@Override
		public void createAndSendDecisionObject(DominionGUI gui,
				Decision decision) {
			gui.makeMultipleChoiceDecision("Do you want to put your deck into your discard pile?", Decision.yesNo.class, null);
		}
	}

	public class Village extends DefaultCard implements ActionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 3; }

		@Override
		public void playCard(Turn turn) {
			turn.drawCards(1);
			turn.addActions(2);
		}
	}

	public class Woodcutter extends DefaultCard implements ActionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 3; }

		@Override
		public void playCard(Turn turn) {
			turn.addBuys(1);
			turn.addBuyingPower(2);
		}
	}

	public class Workshop extends DefaultCard implements DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 3; }

		@Override
		public void playCard(Turn turn) {
			if(turn instanceof ServerTurn) {
				ServerTurn st = (ServerTurn)turn;
				Decision.CardListDecision decision;
				do {
					decision = (Decision.CardListDecision) st.getDecision(this, null);
					// if you tried to gain some number other than 1, it costs more than 4, or the one you wanted 
					// isn't in the supply, request a new one, otherwise we're done
				} while(decision.list.size() != 1 || decision.list.get(0).getCost() > 4 
						|| !st.gainCard(decision.list.get(0)));
			}
		}

		@Override
		public void createAndSendDecisionObject(DominionGUI gui, Decision decision) {
			gui.setupGainCard(4, false, null, null);
		}

		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum, Decision decision, ClientTurn turn) {
			/* server handles sending gain message */
		}
	}

	//fours
	public class Bureaucrat extends VictorySelectionCard implements AttackCard, DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 4; }

		@Override
		public Decision reactToCard(ServerTurn turn) {
			// The turn here is the turn of the reacting player, not the one who played the dominion.card
			int numVictory = 0;
			Card firstVictory = null;
			for(Card c : turn.inHand)
				if(c instanceof VictoryCard) {
					if(numVictory == 0) firstVictory = c;
					numVictory++;
				}
			if(numVictory == 0) turn.revealHand();
			else if(numVictory == 1) turn.putOnDeckFromHand(firstVictory);
			else {
				Decision.CardListDecision decision;
				// there'd better be exactly 1, keep prompting till it is, also must be a victory dominion.card,
				// and in the player's hand
				while(((decision = (Decision.CardListDecision)turn.getDecision(this, null))).list.size() != 1 ||
						!(decision.list.get(0) instanceof VictoryCard) || !turn.inHand.contains(decision.list.get(0)));
				turn.putOnDeckFromHand(decision.list.get(0));
			}
			// don't need to communicate anything back to the caller directly
			return null;
		}

		@Override public void playCard(Turn turn) { 
			if(turn instanceof ServerTurn) {
				// Card.treasureCards[1] is the single instance of Card.Silver
				((ServerTurn) turn).putCardOnTopOfDeck(Card.treasureCards[1]);
				((ServerTurn) turn).doInteraction(this);
			}
			//reaction code takes care of the rest
		}

		//this will be called on the gui of any opponent with multiple victory cards
		@Override public void createAndSendDecisionObject(DominionGUI gui, Decision decision) {
			gui.setupCardSelection(1, true, SelectionType.undraw, this);
		}

		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum, Decision decision, ClientTurn turn) { 
			// server will send message to remove
		}
	}

	public class Feast extends DefaultCard implements DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 4; }

		@Override
		public void playCard(Turn turn) {
			if(turn instanceof ServerTurn) {
				ServerTurn st = (ServerTurn)turn;
				Decision.CardListDecision decision;
				do {
					decision = (Decision.CardListDecision) st.getDecision(this, null);
					// if you tried to gain some number other than 1, it costs more than 5, or the one you wanted 
					// isn't in the supply, request a new one, otherwise we're done and we trash the feast
				} while(decision.list.size() != 1 || decision.list.get(0).getCost() > 5 
						|| !st.gainCard(decision.list.get(0)));
				st.trashCardFromPlay(this);
			}
		}

		@Override
		public void createAndSendDecisionObject(DominionGUI gui, Decision decision) {
			gui.setupGainCard(5, false, null, null);
		}

		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum, Decision decision, ClientTurn turn) {
			/* server handles sending gain message */
		}
	}

	public static class Gardens extends DefaultCard implements ConditionalVictoryCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 4; }
		@Override public int getVictoryPoints() { return 0; }
		@Override public int getVictoryPoints(Stack<Card> deck) { return deck.size()/10; }
	}

	public class Militia extends DefaultCard implements AttackCard, DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 4; }

		@Override
		public Decision reactToCard(ServerTurn turn) {
			// The turn here is the turn of the reacting player, not the one who played the dominion.card
			// if you're already at or below 3 cards, no effect
			if(turn.inHand.size() <= 3) return null;
			
			Decision.CardListDecision decision;
			int numToDiscard = turn.inHand.size() - 3;
			// there'd better be exactly enough to get you down to 3, and you must actually have the cards you sent in your hand
			while(((decision = (Decision.CardListDecision)turn.getDecision(this, new Decision.NumberDecision(numToDiscard)))).list.size()
					!= numToDiscard 
					|| !turn.discardCardsFromHand(decision, this));
			return null;
		}

		@Override public void playCard(Turn turn) { 
			turn.addBuyingPower(2);
			if(turn instanceof ServerTurn) ((ServerTurn) turn).doInteraction(this);
		}

		//this will be called on the gui of any opponent with multiple victory cards
		@Override public void createAndSendDecisionObject(DominionGUI gui, Decision decision) {
			gui.setupCardSelection(((Decision.NumberDecision)decision).num, true, SelectionType.discard, null);
		}

		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum, Decision decision, ClientTurn turn) { 
			// TODO: should this be discard, not trash? look into this.
			gui.trashCardSelection(playerNum, (Decision.CardListDecision)decision);
		}
	}

	public class Moneylender extends DefaultCard implements ActionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 4; }

		@Override
		public void playCard(Turn turn) {
			//TODO maybe each dominion.card should be a singleton with a getInstance() method?
			//TODO should you be allowed to play Moneylender if no copper in hand?
			//		I think it should be ok from dominion.card text, do rules check
			if(turn.containsCard(Card.treasureCards[0])) {
				turn.trashCardFromHand(Card.treasureCards[0]);
				turn.addBuyingPower(3);
			}
		}
	}
	
	public class Remodel extends TreasureSelectionCard implements DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 4; }

		@Override
		public void playCard(Turn turn) {
			if(turn instanceof ServerTurn) {
				ServerTurn st = (ServerTurn)turn;
				Decision.TrashThenGainDecision ttgd = new Decision.TrashThenGainDecision();
				Decision.CardListDecision decision;
				do {
					decision = (Decision.CardListDecision) st.getDecision(this, ttgd);
					// prompt til you get 1 dominion.card that is in the player's hand
				} while(decision.list.size() != 1 || !turn.inHand.contains(decision.list.get(0)));
				Card toTrash = decision.list.get(0);
				st.trashCardFromHand(toTrash);
				st.sendDecisionToPlayer(this, new Decision.ListAndOptionsDecision(ttgd, decision));
				
				ttgd = new Decision.TrashThenGainDecision(toTrash);
				do {
					decision = (Decision.CardListDecision) st.getDecision(this, ttgd);
					// prompt til you get 1 dominion.card that's still available and not too expensive
				} while(decision.list.size() != 1 || decision.list.get(0).getCost() > toTrash.getCost() + 2
						|| !st.gainCard(decision.list.get(0)));
				// the gain happens in the while condition
			}
		}

		@Override
		public void createAndSendDecisionObject(DominionGUI gui, Decision decision) {
			Decision.TrashThenGainDecision dec = (Decision.TrashThenGainDecision) decision;
			if(dec.whichDecision == Decision.TrashThenGainDecision.WhichDecision.chooseTrash) {
				gui.setupCardSelection(1, true, SelectionType.trash, null);
			} else {
				gui.setupGainCard(dec.toTrash.getCost() + 2, false, this, null);
			}
		}

		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum, Decision decision, ClientTurn turn) {
			Decision.ListAndOptionsDecision lod = (Decision.ListAndOptionsDecision) decision;
			Decision.TrashThenGainDecision dec = lod.ttgd;
			if(dec.whichDecision == Decision.TrashThenGainDecision.WhichDecision.chooseTrash) {
				gui.trashCardFromHand(playerNum, lod.cld.list.get(0));
			} else {
				// should never actually get here, no confirmation sent for 
				// this part since it's just a normal gain
			}
		}
	}

	public class Smithy extends DefaultCard implements ActionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 4; }

		@Override
		public void playCard(Turn turn) {
			turn.drawCards(3);
		}
	}

	public class Spy extends NameAndPronounCard implements AttackCard, DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 4; }

		@SuppressWarnings("unchecked")
		@Override
		public void playCard(Turn turn) {
			turn.drawCards(1);
			turn.addActions(1);
			if(turn instanceof ServerTurn) {
				ServerTurn st = (ServerTurn) turn;
				List<Decision> decisions = ((ServerTurn) turn).doInteraction(this);
				//Add in my own dominion.card!
				decisions.add(new Decision.SingleCardDecision(st.revealTopCard()));
				for(int i = 0; i < st.numPlayers(); i++) {
					int playerNum = (st.playerNum() + i + 1)%st.numPlayers();
					if(decisions.get(i) == null) continue; //this means they blocked the attack
					
					Decision d = st.getDecision(this, new Decision.DecisionAndPlayerDecision(decisions.get(i), playerNum));
					if(((Decision.EnumDecision<Decision.keepDiscard>)d).enumValue == Decision.keepDiscard.keep)
						st.getTurn(playerNum).putCardOnTopOfDeck(((Decision.SingleCardDecision)decisions.get(i)).card);
					else st.getTurn(playerNum).discardCardPublically(((Decision.SingleCardDecision)decisions.get(i)).card);
				}
			}
		}

		@Override
		public Decision reactToCard(ServerTurn turn) {
			// this is called on the opponents, but the opponent doesn't need to make any decisions
			return new Decision.SingleCardDecision(turn.revealTopCard());
		}

		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum,
				Decision decision, ClientTurn turn) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void createAndSendDecisionObject(DominionGUI gui, Decision decision) {
			Decision.DecisionAndPlayerDecision dapd = (Decision.DecisionAndPlayerDecision) decision;
			gui.makeMultipleChoiceDecision("" + this.getPlayerName(gui, decision) + " revealed the following dominion.card.  " +
					"Do you want to put it back on " + this.getPronoun(gui, decision) + " deck (keep) or discard it (discard)?", 
					Decision.keepDiscard.class, ((Decision.SingleCardDecision)dapd.decision).card);
		}
	}

	//fives
	public class CouncilRoom extends DefaultCard implements InteractingCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 5; }
		@Override
		public void playCard(Turn turn) {
			turn.drawCards(4);
			turn.addBuys(1);
			if(turn instanceof ServerTurn) ((ServerTurn) turn).doInteraction(this);
		}
		@Override
		public Decision reactToCard(ServerTurn turn) {
			turn.drawCards(1);
			return null;
		}
		
	}
	public class Festival extends DefaultCard implements ActionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 5; }

		@Override
		public void playCard(Turn turn) {
			turn.addActions(2);
			turn.addBuys(1);
			turn.addBuyingPower(2);
		}
	}

	public class Laboratory extends DefaultCard implements ActionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 5; }

		@Override
		public void playCard(Turn turn) {
			turn.drawCards(2);
			turn.addActions(1);
		}
	}

	public class Library extends DefaultCard implements DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 5; }

		// TODO: give error if not the right kind of decision?
		@SuppressWarnings("unchecked")
		@Override
		public void playCard(Turn turn) {
			if(turn instanceof ServerTurn) {
				ServerTurn st = (ServerTurn) turn;
				List<Card> setAside = new ArrayList<Card>();
				while(st.inHand.size() < 7) {
					Card c = st.lookAtTopCard(); // pops top dominion.card off deck
					if(c == null) break; // all cards are in hand or set-aside list, no more to draw
					if(c instanceof ActionCard) {
						Decision d = ((ServerTurn) turn).getDecision(this, new Decision.SingleCardDecision(c));
						if(((Decision.EnumDecision<Decision.keepDiscard>)d).enumValue == Decision.keepDiscard.discard) {
							setAside.add(c);
							continue;
						}
					}
					st.putCardInHand(c);
				}
				for(Card c : setAside) st.discardCard(c);
			}
		}

		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum,
				Decision decision, ClientTurn turn) {
			// Note: nothing to do here unless we add visuals for "set aside"
		}

		@Override
		public void createAndSendDecisionObject(DominionGUI gui,
				Decision decision) {
			gui.makeMultipleChoiceDecision("Do you want to set aside this action (discard) or draw it into your hand (keep)?", 
						Decision.keepDiscard.class, ((Decision.SingleCardDecision)decision).card);
		}
	}

	public class Market extends DefaultCard implements ActionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 5; }

		@Override
		public void playCard(Turn turn) {
			turn.drawCards(1);
			turn.addActions(1);
			turn.addBuys(1);
			turn.addBuyingPower(1);
		}
	}

	public class Mine extends TreasureSelectionCard implements DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 5; }

		@Override
		public void playCard(Turn turn) {
			if(turn instanceof ServerTurn) {
				ServerTurn st = (ServerTurn)turn;
				Decision.TrashThenGainDecision ttgd = new Decision.TrashThenGainDecision();
				Decision.CardListDecision decision;
				do {
					decision = (Decision.CardListDecision) st.getDecision(this, ttgd);
					// if you tried to trash some number other than 1, or it's not a treasure, 
					// request a new one, otherwise we move on to the gaining bit
				} while(decision.list.size() != 1 || !(decision.list.get(0) instanceof TreasureCard)
						|| !turn.inHand.contains(decision.list.get(0)));
				TreasureCard toTrash = (TreasureCard) decision.list.get(0);
				st.trashCardFromHand(toTrash);
				st.sendDecisionToPlayer(this, new Decision.ListAndOptionsDecision(ttgd, decision));
				
				ttgd = new Decision.TrashThenGainDecision(toTrash);
				do {
					decision = (Decision.CardListDecision) st.getDecision(this, ttgd);
					// if you tried to gain some number other than 1, or it's not a treasure, or none are left 
					// request a new one, otherwise go ahead and gain it!
				} while(decision.list.size() != 1 || !(decision.list.get(0) instanceof TreasureCard)
						|| decision.list.get(0).getCost() > 3 + toTrash.getCost()
						|| !st.gainCardToHand(decision.list.get(0)));
				st.sendDecisionToPlayer(this, new Decision.ListAndOptionsDecision(ttgd, decision));
			}
		}

		@Override
		public void createAndSendDecisionObject(DominionGUI gui, Decision decision) {
			Decision.TrashThenGainDecision dec = (Decision.TrashThenGainDecision) decision;
			if(dec.whichDecision == Decision.TrashThenGainDecision.WhichDecision.chooseTrash) {
				gui.setupCardSelection(1, false, SelectionType.trash, this);
			} else {
				gui.setupGainCard(dec.toTrash.getCost() + 3, false, this, null);
			}
		}

		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum, Decision decision, ClientTurn turn) {
			Decision.ListAndOptionsDecision lod = (Decision.ListAndOptionsDecision) decision;
			Decision.TrashThenGainDecision dec = lod.ttgd;
			if(dec.whichDecision == Decision.TrashThenGainDecision.WhichDecision.chooseTrash) {
				gui.trashCardFromHand(playerNum, lod.cld.list.get(0));
			} else {
				gui.addCardToHand(playerNum, lod.cld.list.get(0));
			}
		}
	}

	public class Witch extends DefaultCard implements AttackCard {
		private static final long serialVersionUID = 1L;

		@Override
		public Decision reactToCard(ServerTurn turn) {
			turn.gainCurse();
			return null;
		}

		@Override public void playCard(Turn turn) { 
			turn.drawCards(2); 
			if(turn instanceof ServerTurn) ((ServerTurn) turn).doInteraction(this);
		}
		@Override public int getCost() { return 5; }
	}

	public class Adventurer extends DefaultCard implements ActionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 6; }

		@Override
		public void playCard(Turn turn) {
			if(turn instanceof ServerTurn) {
				ServerTurn st = (ServerTurn)turn;
				
				List<Card> revealedCards = new ArrayList<Card>();
				List<Card> treasureToPickUp = new ArrayList<Card>();
				// reveal cards one at a time until you get 2 treasures
				while(treasureToPickUp.size()<2) {
					Card c = st.revealTopCard();
					if(c instanceof TreasureCard) {
						treasureToPickUp.add(c);
						// this sends a message to all the players
						st.putCardInHand(c);
					} else
						revealedCards.add(c);
				}
				// now discard all the revealed cards
				for(Card c : revealedCards)
					st.discardCard(c);
			}
		}
	}

	//Intrigue
	public class Courtyard extends DefaultCard implements DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 2; }

		@Override
		public void playCard(Turn turn) {
			turn.drawCards(3);
			if(turn instanceof ServerTurn) {  
				ServerTurn st = (ServerTurn)turn;
				Decision.CardListDecision decision;
				do {
					decision = (Decision.CardListDecision) st.getDecision(this, null);
					// if you tried to put back some number other than 1, or it's not in your hand
					// request a new one, otherwise we're done 
				} while(decision.list.size() != 1 || !st.putOnDeckFromHand(decision.list.get(0)));
			}
			//wait for processing code to ask for discard
		}

		@Override
		public void createAndSendDecisionObject(DominionGUI gui, Decision decision) {
			gui.setupCardSelection(1, true, SelectionType.undraw, null);
		}

		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum, Decision decision, ClientTurn turn) {
			//server will send message to make it happen
		}

	}

	public class GreatHall extends DefaultCard implements ActionCard, VictoryCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 3; }

		@Override
		public void playCard(Turn turn) {
			turn.drawCards(1);
			turn.addActions(1);
		}

		@Override public int getVictoryPoints() {	return 1; }
	}

	public class ShantyTown extends DefaultCard implements ActionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 3; }

		@Override
		public void playCard(Turn turn) {
			turn.addActions(2);
			if(!turn.actionsInHand()) {
				turn.revealHand();
				turn.drawCards(2);
			}
		}
	}

	public class Steward extends DefaultCard implements DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 3; }

		// TODO: give error if not the right kind of decision?
		@SuppressWarnings("unchecked")
		@Override
		public void playCard(Turn turn) {
			if(turn instanceof ServerTurn) {
				Decision d = ((ServerTurn) turn).getDecision(this, new Decision.EnumDecision<Decision.firstSecond>(Decision.firstSecond.first));
				if(((Decision.EnumDecision<Decision.stewardDecision>)d).enumValue == Decision.stewardDecision.draw)
					turn.drawCards(2);
				else if(((Decision.EnumDecision<Decision.stewardDecision>)d).enumValue == Decision.stewardDecision.money)
					turn.addBuyingPower(2);
				else if(((Decision.EnumDecision<Decision.stewardDecision>)d).enumValue == Decision.stewardDecision.trash) {
					Decision.CardListDecision decision;
					do {
						//request decision
						decision = (Decision.CardListDecision) ((ServerTurn)turn).getDecision(this, new Decision.EnumDecision<Decision.firstSecond>(Decision.firstSecond.second));
						//try using this decision, if it doesn't work, ask again
					} while(decision.list.size() != 2 || !((ServerTurn)turn).trashCardsFromHand(decision, this));
					// trashCardsFromHands sends a confirmation to the client
				}
				((ServerTurn)turn).sendDecisionToPlayer(this, d);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum,
				Decision decision, ClientTurn turn) {
			if(decision instanceof Decision.CardListDecision)
				gui.trashCardSelection(playerNum, (Decision.CardListDecision)decision);
			else if(decision instanceof Decision.EnumDecision<?> && ((Decision.EnumDecision<?>)decision).enumValue instanceof Decision.stewardDecision){
				if(((Decision.EnumDecision<Decision.stewardDecision>)decision).enumValue == Decision.stewardDecision.draw)
					turn.drawCards(2);
				else if(((Decision.EnumDecision<Decision.stewardDecision>)decision).enumValue == Decision.stewardDecision.money)
					turn.addBuyingPower(2);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void createAndSendDecisionObject(DominionGUI gui,
				Decision decision) {
			if(((Decision.EnumDecision<Decision.firstSecond>)decision).enumValue == Decision.firstSecond.first)
				gui.makeMultipleChoiceDecision("Do you want to trash 2 cards, draw two cards, or gain 2 coin?", Decision.stewardDecision.class, null);
			else
				gui.setupCardSelection(2, true, SelectionType.trash, null);
		}
	}

	public class Swindler extends NameAndPronounCard implements AttackCard, DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 3; }

		@Override
		public Decision reactToCard(ServerTurn turn) {
			// The turn here is the turn of the reacting player, not the one who played the dominion.card
			Card topCard = turn.revealTopCard();
			turn.trashCard(topCard);
			return new Decision.SingleCardDecision(topCard);
		}

		@Override public void playCard(Turn turn) { 
			turn.addBuyingPower(2);
			if(turn instanceof ServerTurn) {
				ServerTurn st = (ServerTurn) turn;
				List<Decision> decisions = st.doInteraction(this);
				
				for(int i = 0; i < st.numPlayers() - 1; i++) {
					int playerNum = (st.playerNum() + i + 1)%st.numPlayers();
					if(decisions.get(i) == null) continue; //this means they blocked the attack
					Card topCard = ((Decision.SingleCardDecision)decisions.get(i)).card;
					
					// if no cards of that cost exist, just move along
					if(!st.supplyContainsExactCost(topCard.getCost())) continue;
					
					// prompt player for another dominion.card from supply of the same cost for the attacked player to gain
					Decision.CardListDecision decision;
					do {
						decision = (Decision.CardListDecision) st.getDecision(this, new Decision.DecisionAndPlayerDecision(decisions.get(i), playerNum));
					} while(decision.list.size() != 1 || decision.list.get(0).getCost() != topCard.getCost() 
							|| !st.getTurn(playerNum).gainCard(decision.list.get(0)));
				}
			}
			//reaction code takes care of the rest
		}

		//this will be called on the gui of all opponents (unless they have a moat/lighthouse)
		@Override public void createAndSendDecisionObject(DominionGUI gui, Decision decision) {
			Decision.DecisionAndPlayerDecision dapd = (Decision.DecisionAndPlayerDecision) decision;
			String message = "" + this.getPlayerName(gui, decision) + " trashed the following dominion.card: "
				+ ((Decision.SingleCardDecision)dapd.decision).card + ".  Choose a dominion.card of the same cost to replace it.";
			gui.setupGainCard(((Decision.SingleCardDecision)dapd.decision).card.getCost(), true, null, message);
		}

		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum, Decision decision, ClientTurn turn) { 
			// server will send message to gain
		}
	}

	public class Baron extends DefaultCard implements DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 4; }

		@SuppressWarnings("unchecked")
		@Override
		// TODO: perhaps make a setting that allows you to always discard the estate if you
		// have one?  Going strictly by the rules I have to ask, but as a player it's super
		// annoying and I'd never play it to gain an estate when I have one I could discard.
		public void playCard(Turn turn) {
			turn.addBuys(1);
			if(turn instanceof ServerTurn) {
				ServerTurn st = (ServerTurn)turn;
				if(st.containsCard(Card.victoryCards[0])) {
					Decision d = null;
					while(d == null || !(d instanceof Decision.EnumDecision<?>))
						d = (Decision.EnumDecision<Decision.yesNo>) ((ServerTurn) turn).getDecision(this, null);
					st.sendDecisionToPlayer(this, d); //auto
					if(((Decision.EnumDecision<Decision.yesNo>)d).enumValue == Decision.yesNo.yes) {
						st.discardCardFromHand(Card.victoryCards[0]);
						st.addBuyingPower(4);
						return;
					}
				}
				st.gainCard(Card.victoryCards[0]);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum,
				Decision decision, ClientTurn turn) {
			if(((Decision.EnumDecision<Decision.yesNo>)decision).enumValue == Decision.yesNo.yes) {
				turn.discardCardFromHand(Card.victoryCards[0]);
				turn.addBuyingPower(4);
			}
			// Note: if the answer was no, the server deals with gaining the estate
		}

		@Override
		public void createAndSendDecisionObject(DominionGUI gui,
				Decision decision) {
			gui.makeMultipleChoiceDecision("Do you want to discard an Estate?", Decision.yesNo.class, null);
		}
	}

	public class Conspirator extends DefaultCard implements ActionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 4; }

		@Override
		public void playCard(Turn turn) {
			turn.addBuyingPower(2);
			if(turn.inPlay.size() > 2) {
				turn.drawCards(1);
				turn.addActions(1);
			}
		}
	}
	
	public class Coppersmith extends DefaultCard implements ActionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 4; }

		@Override
		public void playCard(Turn turn) {
			if(turn instanceof ServerTurn)
				((ServerTurn) turn).addCoppersmith();
		}
	}

	public class Ironworks extends SwitchByType implements DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 4; }

		@Override
		public void playCard(Turn turn) {
			if(turn instanceof ServerTurn) {
				ServerTurn st = (ServerTurn)turn;
				Decision.CardListDecision decision;
				do {
					decision = (Decision.CardListDecision) st.getDecision(this, null);
					// if you tried to gain some number other than 1, it costs more than 4, or the one you wanted 
					// isn't in the supply, request a new one, otherwise we're done and we reap the benefits
				} while(decision.list.size() != 1 || decision.list.get(0).getCost() > 4 
						|| !st.gainCard(decision.list.get(0)));
				switchHelper(turn, decision, 1, 1);
				st.sendDecisionToPlayer(this, decision);
			}
		}


		@Override
		public void createAndSendDecisionObject(DominionGUI gui, Decision decision) {
			gui.setupGainCard(4, false, null, null);
		}

		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum, Decision decision, ClientTurn turn) {
			/* server handles sending gain message, so don't worry about that bit here */
			switchHelper(turn, decision, 1, 1);
		}
	}

	public class MiningVillage extends DefaultCard implements DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 4; }

		@SuppressWarnings("unchecked")
		@Override
		public void playCard(Turn turn) {
			turn.drawCards(1);
			turn.addActions(2);
			Decision d = null;
			if(turn instanceof ServerTurn) {
				ServerTurn st = (ServerTurn) turn;
				while(d == null || !(d instanceof Decision.EnumDecision<?>))
					d = (Decision.EnumDecision<Decision.yesNo>) ((ServerTurn) turn).getDecision(this, null);
				st.sendDecisionToPlayer(this, d);
				if(((Decision.EnumDecision<Decision.yesNo>)d).enumValue == Decision.yesNo.yes) {
					st.trashCardFromPlay(this);
					st.addBuyingPower(2);
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum,
				Decision decision, ClientTurn turn) {
			if(((Decision.EnumDecision<Decision.yesNo>)decision).enumValue == Decision.yesNo.yes) {
				turn.trashCardFromPlay(this);
				turn.addBuyingPower(2);
			}
		}
		
		@Override
		public void createAndSendDecisionObject(DominionGUI gui,
				Decision decision) {
			gui.makeMultipleChoiceDecision("Do you want to trash this dominion.card for 2 coin?", Decision.yesNo.class, null);
		}
	}

	public class SeaHag extends DefaultCard implements AttackCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 4; }

		@Override
		public Decision reactToCard(ServerTurn turn) {
			turn.discardCard(turn.revealTopCard());
			turn.putCardOnTopOfDeck(Card.curse);
			return null;
		}

		@Override public void playCard(Turn turn) { 
			if(turn instanceof ServerTurn) ((ServerTurn) turn).doInteraction(this);
		}
	}


	public static class Duke extends DefaultCard implements ConditionalVictoryCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 5; }
		@Override public int getVictoryPoints() { return 0; }
		@Override 
		public int getVictoryPoints(Stack<Card> deck) { 
			int count = 0;
			for(Card c : deck) {
				if(c instanceof Duchy) count++;
			}
			return count;
		}
	}

	public class Minion extends DefaultCard implements DecisionCard, AttackCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 5; }

		@SuppressWarnings("unchecked")
		@Override
		public void playCard(Turn turn) {
			turn.addActions(1);
			if(turn instanceof ServerTurn) {
				ServerTurn st = (ServerTurn) turn;
				Decision d = st.getDecision(this, null);
				if(((Decision.EnumDecision<Decision.minionDecision>)d).enumValue == Decision.minionDecision.money)
					turn.addBuyingPower(2);
				else if(((Decision.EnumDecision<Decision.minionDecision>)d).enumValue == Decision.minionDecision.redraw) {
					st.discardHand();
					st.drawCards(4);
					st.doInteraction(this);
				}
				// note: not strictly necessary, since the buyingPower calculated
				// on the client side is never used...
				st.sendDecisionToPlayer(this, d);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum, Decision decision, ClientTurn turn) {
			if(((Decision.EnumDecision<Decision.minionDecision>)decision).enumValue == Decision.minionDecision.money)
				turn.addBuyingPower(2);
		}

		@Override
		public void createAndSendDecisionObject(DominionGUI gui, Decision decision) {
			gui.makeMultipleChoiceDecision("Do you want 2 coin, or do you want to discard your hand and draw 4 new cards, attacking the other players?", Decision.minionDecision.class, null);
		}

		@Override
		public Decision reactToCard(ServerTurn turn) {
			if(turn.inHand.size() >= 5) {
				turn.discardHand();
				turn.drawCards(4);
			}
			return null;
		}
	}

	public class Tribute extends SwitchByType implements InteractingCard, DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 5; }

		@Override
		public void playCard(Turn turn) {
			if(turn instanceof ServerTurn) ((ServerTurn) turn).doInteraction(this);
		}

		@Override
		public Decision reactToCard(ServerTurn turn) {
			//if you are player to the left, send your top two cards
			if((turn.currentPlayer() + 1)%turn.numPlayers() == turn.playerNum()) {
				ArrayList<Card> list = new ArrayList<Card>();
				for(int i = 0; i < 2; i++) list.add(turn.revealTopCard());
				for(int i = 0; i < 2; i++) turn.discardCard(list.get(i));
				Decision.CardListDecision cld = new Decision.CardListDecision(list);
				//just do do it directly here, and send a decision confirmation to the current player
				switchHelper(turn.currentTurn(), cld, 2, 2);
				turn.currentTurn().sendDecisionToPlayer(this, cld);
			}
			//everyone else just ignores it
			return null;
		}

		@Override
		public void createAndSendDecisionObject(DominionGUI gui, Decision decision) {
			// no decision needed from the GUI for this dominion.card
		}

		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum, Decision decision, ClientTurn turn) {
			switchHelper(turn, decision, 2, 2);
		}

	}

	public class Upgrade extends TreasureSelectionCard implements DecisionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 5; }

		@Override
		public void playCard(Turn turn) {
			turn.drawCards(1);
			turn.addActions(1);
			if(turn instanceof ServerTurn) {
				ServerTurn st = (ServerTurn)turn;
				Decision.TrashThenGainDecision ttgd = new Decision.TrashThenGainDecision();
				Decision.CardListDecision decision;
				do {
					decision = (Decision.CardListDecision) st.getDecision(this, ttgd);
					// prompt til you get 1 dominion.card that is in the player's hand
				} while(decision.list.size() != 1 || !turn.inHand.contains(decision.list.get(0)));
				Card toTrash = decision.list.get(0);
				st.trashCardFromHand(toTrash);
				st.sendDecisionToPlayer(this, new Decision.ListAndOptionsDecision(ttgd, decision));

				// if there are no cards costing exactly one more, we're done, nothing left to do
				// TODO: maybe send a message telling the user there were no cards with that price
				// to gain so they don't get anything?
				if(!st.supplyContainsExactCost(toTrash.getCost() + 1)) return;
				
				// otherwise, gain one!
				ttgd = new Decision.TrashThenGainDecision(toTrash);
				do {
					
					decision = (Decision.CardListDecision) st.getDecision(this, ttgd);
					// prompt til you get 1 dominion.card that's not too expensive and still available
				} while(decision.list.size() != 1 || decision.list.get(0).getCost() != toTrash.getCost() + 1 
						|| !st.gainCard(decision.list.get(0)));
				// the gain happens in the while condition
			}
		}

		@Override
		public void createAndSendDecisionObject(DominionGUI gui, Decision decision) {
			Decision.TrashThenGainDecision dec = (Decision.TrashThenGainDecision) decision;
			if(dec.whichDecision == Decision.TrashThenGainDecision.WhichDecision.chooseTrash) {
				gui.setupCardSelection(1, true, SelectionType.trash, null);
			} else {
				gui.setupGainCard(dec.toTrash.getCost() + 1, true, this, null);
			}
		}

		@Override
		public void carryOutDecision(DominionGUI gui, int playerNum, Decision decision, ClientTurn turn) {
			Decision.ListAndOptionsDecision lod = (Decision.ListAndOptionsDecision) decision;
			Decision.TrashThenGainDecision dec = lod.ttgd;
			if(dec.whichDecision == Decision.TrashThenGainDecision.WhichDecision.chooseTrash) {
				gui.trashCardFromHand(playerNum, lod.cld.list.get(0));
			} else {
				// we don't actually get a message for this
			}
		}
	}

	public class Harem extends DefaultCard implements VictoryCard, TreasureCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 6; }
		@Override public int getVictoryPoints() { return 2; }
		@Override public int getValue() { return 2; }
	}


	//Seaside
	public class Bazaar extends DefaultCard implements ActionCard {
		private static final long serialVersionUID = 1L;
		@Override public int getCost() { return 5; }

		@Override
		public void playCard(Turn turn) {
			turn.drawCards(1);
			turn.addActions(2);
			turn.addBuyingPower(1);
		}
	}	

}
