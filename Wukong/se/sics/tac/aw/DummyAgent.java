/**
 * TAC AgentWare
 * http://www.sics.se/tac        tac-dev@sics.se
 *
 * Copyright (c) 2001-2005 SICS AB. All rights reserved.
 *
 * SICS grants you the right to use, modify, and redistribute this
 * software for noncommercial purposes, on the conditions that you:
 * (1) retain the original headers, including the copyright notice and
 * this text, (2) clearly document the difference between any derived
 * software and the original, and (3) acknowledge your use of this
 * software in pertaining publications and reports.  SICS provides
 * this software "as is", without any warranty of any kind.  IN NO
 * EVENT SHALL SICS BE LIABLE FOR ANY DIRECT, SPECIAL OR INDIRECT,
 * PUNITIVE, INCIDENTAL OR CONSEQUENTIAL LOSSES OR DAMAGES ARISING OUT
 * OF THE USE OF THE SOFTWARE.
 *
 * -----------------------------------------------------------------
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : 23 April, 2002
 * Updated : $Date: 2005/06/07 19:06:16 $
 *	     $Revision: 1.1 $
 * ---------------------------------------------------------
 * DummyAgent is a simplest possible agent for TAC. It uses
 * the TACAgent agent ware to interact with the TAC server.
 *
 * Important methods in TACAgent:
 *
 * Retrieving information about the current Game
 * ---------------------------------------------
 * int getGameID()
 *  - returns the id of current game or -1 if no game is currently plaing
 *
 * getServerTime()
 *  - returns the current server time in milliseconds
 *
 * getGameTime()
 *  - returns the time from start of game in milliseconds
 *
 * getGameTimeLeft()
 *  - returns the time left in the game in milliseconds
 *
 * getGameLength()
 *  - returns the game length in milliseconds
 *
 * int getAuctionNo()
 *  - returns the number of auctions in TAC
 *
 * int getClientPreference(int client, int type)
 *  - returns the clients preference for the specified type
 *   (types are TACAgent.{ARRIVAL, DEPARTURE, HOTEL_VALUE, E1, E2, E3}
 *
 * int getAuctionFor(int category, int type, int day)
 *  - returns the auction-id for the requested resource
 *   (categories are TACAgent.{CAT_FLIGHT, CAT_HOTEL, CAT_ENTERTAINMENT
 *    and types are TACAgent.TYPE_INFLIGHT, TACAgent.TYPE_OUTFLIGHT, etc)
 *
 * int getAuctionCategory(int auction)
 *  - returns the category for this auction (CAT_FLIGHT, CAT_HOTEL,
 *    CAT_ENTERTAINMENT)
 *
 * int getAuctionDay(int auction)
 *  - returns the day for this auction.
 *
 * int getAuctionType(int auction)
 *  - returns the type for this auction (TYPE_INFLIGHT, TYPE_OUTFLIGHT, etc).
 *
 * int getOwn(int auction)
 *  - returns the number of items that the agent own for this
 *    auction
 *
 * Submitting Bids
 * ---------------------------------------------
 * void submitBid(Bid)
 *  - submits a bid to the tac server
 *
 * void replaceBid(OldBid, Bid)
 *  - replaces the old bid (the current active bid) in the tac server
 *
 *   Bids have the following important methods:
 *    - create a bid with new Bid(AuctionID)
 *
 *   void addBidPoint(int quantity, float price)
 *    - adds a bid point in the bid
 *
 * Help methods for remembering what to buy for each auction:
 * ----------------------------------------------------------
 * int getAllocation(int auctionID)
 *   - returns the allocation set for this auction
 * void setAllocation(int auctionID, int quantity)
 *   - set the allocation for this auction
 *
 *
 * Callbacks from the TACAgent (caused via interaction with server)
 *
 * bidUpdated(Bid bid)
 *  - there are TACAgent have received an answer on a bid query/submission
 *   (new information about the bid is available)
 * bidRejected(Bid bid)
 *  - the bid has been rejected (reason is bid.getRejectReason())
 * bidError(Bid bid, int error)
 *  - the bid contained errors (error represent error status - commandStatus)
 *
 * quoteUpdated(Quote quote)
 *  - new information about the quotes on the auction (quote.getAuction())
 *    has arrived
 * quoteUpdated(int category)
 *  - new information about the quotes on all auctions for the auction
 *    category has arrived (quotes for a specific type of auctions are
 *    often requested at once).

 * auctionClosed(int auction)
 *  - the auction with id "auction" has closed
 *
 * transaction(Transaction transaction)
 *  - there has been a transaction
 *
 * gameStarted()
 *  - a TAC game has started, and all information about the
 *    game is available (preferences etc).
 *
 * gameStopped()
 *  - the current game has ended
 *
 */

package se.sics.tac.aw;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.Timer;

import java.awt.event.*;

import se.sics.tac.util.ArgEnumerator;

public class DummyAgent extends AgentImpl {

  private static final Logger log =
    Logger.getLogger(DummyAgent.class.getName());

  private static final boolean DEBUG = false;

  private float[] prices;
  
  private Timer updateTimer;
  
  private Bid[] oldBids = new Bid[28];
  
//  private int[][] clientPackages = new int[8][3];
//  
//  private int[] inCalendar = {0,0,0,0,0};


  protected void init(ArgEnumerator args) {
    prices = new float[agent.getAuctionNo()];
  }

  public void quoteUpdated(Quote quote) {
	  int auction = quote.getAuction(); // get the auction no.
	  int auctionCategory = agent.getAuctionCategory(auction); // get the category of auction
	  if (auctionCategory == TACAgent.CAT_HOTEL) {
		  // check for hotel auction
		  int alloc = agent.getAllocation(auction); // return the value of allocate[auction]
		  if (alloc > 0 && quote.hasHQW(agent.getBid(auction)) && quote.getHQW() < alloc) {
			  // check if could send a new bid
			  Bid bid = new Bid(auction);
			  // Can not own anything in hotel auctions...
			  prices[auction] = quote.getAskPrice() + 50; // set the prices of this auction
			  bid.addBidPoint(alloc, prices[auction]); // add a new bid into queue
			  if (DEBUG) {
				  log.finest("submitting bid with alloc="
						  + agent.getAllocation(auction)
						  + " own=" + agent.getOwn(auction));
			  }
			  agent.submitBid(bid); // submit bid
			  //agent.replaceBid(oldBids[auction], bid);
		  }
	  }
	  else if (auctionCategory == TACAgent.CAT_ENTERTAINMENT) {
		  // check for entertainment auction
		  int alloc = agent.getAllocation(auction) - agent.getOwn(auction); // need - own
		  if (alloc != 0) {
			  Bid bid = new Bid(auction);
			  if (alloc < 0) {
				  if (agent.getGameTime()>4*60*1000) {
					  prices[auction] = quote.getBidPrice()-10f;
				  }
				  else {
					  prices[auction] = (float) (quote.getAskPrice()-0.1); // sell price
				  }
			  }
			  else
				  prices[auction] = 50f + (agent.getGameTime() * 100f) / 540000; // buy price
			  bid.addBidPoint(alloc, prices[auction]);
			  if (DEBUG) {
				  log.finest("submitting bid with alloc="
						  + agent.getAllocation(auction)
						  + " own=" + agent.getOwn(auction));
			  }
			  agent.submitBid(bid);
		  }
	  }
//	  else if (auctionCategory == TACAgent.CAT_FLIGHT) {
//		  int alloc = agent.getAllocation(auction) - agent.getOwn(auction);
//		  if (alloc > 0) {
//			  Bid bid = new Bid(auction);
//			  if(agent.getGameTime() > 360000.0)
//				  prices[auction] += 20f;
//			  bid.addBidPoint(alloc, prices[auction]);
//			  if (DEBUG) {
//				  log.finest("replacing bid with alloc="
//						  + agent.getAllocation(auction)
//						  + " own=" + agent.getOwn(auction));
//			  }
//			  //agent.submitBid(bid);
//			  agent.replaceBid(agent.getBid(auction), bid);
//		  }
//	  }
  }


  public void quoteUpdated(int auctionCategory) {
    log.fine("All quotes for "
	     + agent.auctionCategoryToString(auctionCategory)
	     + " has been updated");
  }

  public void bidUpdated(Bid bid) {
    log.fine("Bid Updated: id=" + bid.getID() + " auction="
	     + bid.getAuction() + " state="
	     + bid.getProcessingStateAsString());
    log.fine("       Hash: " + bid.getBidHash());
  }

  public void bidRejected(Bid bid) {
    log.warning("Bid Rejected: " + bid.getID());
    log.warning("      Reason: " + bid.getRejectReason()
		+ " (" + bid.getRejectReasonAsString() + ')');
  }

  public void bidError(Bid bid, int status) {
    log.warning("Bid Error in auction " + bid.getAuction() + ": " + status
		+ " (" + agent.commandStatusToString(status) + ')');
  }

  public void gameStarted() {
    log.fine("Game " + agent.getGameID() + " started!");

    //collectPreference();
    
    //randomChanging();
    
    calculateAllocation();
    
    
    sendBids();
    
//    ActionListener updateAllocation = new ActionListener() {
//    	public void actionPerformed(ActionEvent evt) {
//    		
//    	}
//    };
////     
//    // callback calculateAllocation every 1 minute
//    updateTimer = new Timer(1*60*1000, updateAllocation);
//	updateTimer.start();
  }

  public void gameStopped() {
	  
	updateTimer.stop(); // stop allocation updating
	  
    log.fine("Game Stopped!");
  }

  public void auctionClosed(int auction) {
    log.fine("*** Auction " + auction + " closed!");
  }

  private void sendBids() {
	  for (int i = 0, n = agent.getAuctionNo(); i < n; i++) {
		  int alloc = agent.getAllocation(i) - agent.getOwn(i);
		  float price = -1f;
		  switch (agent.getAuctionCategory(i)) {
      		case TACAgent.CAT_FLIGHT:
      			if (alloc > 0) {
      				price = 450;
      				prices[i] = 450f;
      			}
      			break;
      		case TACAgent.CAT_HOTEL:
      			if (alloc > 0) {
      				price = 500;
      				prices[i] = 500f;
      			}
      			break;
      		case TACAgent.CAT_ENTERTAINMENT:
      			if (alloc < 0) {
      				price = 248;
      				prices[i] = 248f;
      			}
      			else if (alloc > 0) {
      				price = 50;
      				prices[i] = 50f;
      			}
      			break;
      		default:
      			break;
		  }
		  if (price > 0) {
			  Bid bid = new Bid(i);
			  bid.addBidPoint(alloc, price);
			  if (DEBUG) {
				  log.finest("submitting bid with alloc=" + agent.getAllocation(i)
						  + " own=" + agent.getOwn(i));
			  }
			  agent.submitBid(bid);
		  }
	  }
  }
  
//  private void collectPreference() {
//	  for (int i = 0; i < 8; i++) {
//		  clientPackages[i][0] = agent.getClientPreference(i, TACAgent.ARRIVAL);
//		  clientPackages[i][1] = agent.getClientPreference(i, TACAgent.DEPARTURE);
//		  clientPackages[i][2] = agent.getClientPreference(i, TACAgent.HOTEL_VALUE);
//	  }
//  }
//  
//  private void randomChanging() {
//	  for (int i=0; i<8; i++) {
//		  inCalendar[clientPackages[i][0]-1] += 1;
//	  }
//	  for(int i=0; i<=5; i++) {
//		 if (inCalendar[i]>3) {
//			 for (int j=0; j<8; j++) {
//				 if(clientPackages[j][0] == i+1 && clientPackages[j][1]-clientPackages[j][0]>1) {
//					 clientPackages[j][0] -= 1;
//					 clientPackages[j+1][0] += 1;
//					 inCalendar[i]--;
//				 }
//				 if(inCalendar[i]<=3) {
//					 break;
//				 }
//			 }
//		 }
//	  }
//  }
  
  private void calculateAllocation() {
	  
	  for (int i = 0; i < 8; i++) {
//		  int inFlight = clientPackages[i][0];
//		  int outFlight = clientPackages[i][1];
//		  int hotel = clientPackages[i][2];
		  
		  int inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL);
		  int outFlight = agent.getClientPreference(i, TACAgent.DEPARTURE);
		  int hotel = agent.getClientPreference(i, TACAgent.HOTEL_VALUE);
		  int type;
		  
		  int auction;
		  

		  // Get the flight preferences auction and remember that we are
		  // going to buy tickets for these days. (inflight=1, outflight=0)
		  // category, type, day
		  auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT, TACAgent.TYPE_INFLIGHT, inFlight);
		  agent.setAllocation(auction, agent.getAllocation(auction) + 1);
		  auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT, TACAgent.TYPE_OUTFLIGHT, outFlight);
		  agent.setAllocation(auction, agent.getAllocation(auction) + 1);

		  // expensive hotel (type = 1)
		  if (hotel > 86) {
			  type = TACAgent.TYPE_GOOD_HOTEL;
		  }
		  else {
			  type = TACAgent.TYPE_CHEAP_HOTEL;
		  }
		  // allocate a hotel night for each day that the agent stays
		  for (int d = inFlight; d < outFlight; d++) {
			  auction = agent.getAuctionFor(TACAgent.CAT_HOTEL, type, d);
			  log.finer("Adding hotel for day: " + d + " on " + auction);
			  agent.setAllocation(auction, agent.getAllocation(auction) + 1);
		  }

//		  int eType = -1;
//		  while((eType = nextEntType(i, eType)) > 0) {
//			  auction = bestEntDay(inFlight, outFlight, eType);
//			  log.finer("Adding entertainment " + eType + " on " + auction);
//			  agent.setAllocation(auction, agent.getAllocation(auction) + 1);
//		  }
		  
		  int EntType;
		     int d = outFlight - inFlight;
		     if(d>=3){
		       d=3;
		     }
		   for(int ord=1;ord<=d;ord++){
			   EntType = nextEntType(i, ord);
		       auction = agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,
		    		   EntType, inFlight+ord-1);
		       agent.setAllocation(auction, agent.getAllocation(auction) + 1);
		   }
	  }
  }

  private int bestEntDay(int inFlight, int outFlight, int type) {
    for (int i = inFlight; i < outFlight; i++) {
      int auction = agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,
					type, i);
      if (agent.getAllocation(auction) < agent.getOwn(auction)) {
	return auction;
      }
    }
    // If no left, just take the first...
    return agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,
			       type, inFlight);
  }

//  private int nextEntType(int client, int lastType) {
//    int e1 = agent.getClientPreference(client, TACAgent.E1);
//    int e2 = agent.getClientPreference(client, TACAgent.E2);
//    int e3 = agent.getClientPreference(client, TACAgent.E3);
//
//    // At least buy what each agent wants the most!!!
//    if ((e1 > e2) && (e1 > e3) && lastType == -1)
//      return TACAgent.TYPE_ALLIGATOR_WRESTLING;
//    if ((e2 > e1) && (e2 > e3) && lastType == -1)
//      return TACAgent.TYPE_AMUSEMENT;
//    if ((e3 > e1) && (e3 > e2) && lastType == -1)
//      return TACAgent.TYPE_MUSEUM;
//    return -1;
//  }
  
  private int nextEntType(int client, int order) {
      int e1 = agent.getClientPreference(client, TACAgent.E1);
      int e2 = agent.getClientPreference(client, TACAgent.E2);
      int e3 = agent.getClientPreference(client, TACAgent.E3);
      switch(order){
      case 1:
        if(e1 > e2 && e1 > e3)
          return TACAgent.TYPE_ALLIGATOR_WRESTLING;
        else if (e2 > e1 && e2 > e3)
          return TACAgent.TYPE_AMUSEMENT;
        else if (e3 > e1 && e3 > e2)
          return TACAgent.TYPE_MUSEUM;
      case 2:
        if((e1 > e2 && e1 < e3)||(e1 < e2 &&e1 > e3))
          return TACAgent.TYPE_ALLIGATOR_WRESTLING;
        else if ((e2 > e1 && e2 < e3)||(e2 < e1 && e2 > e3))
          return TACAgent.TYPE_AMUSEMENT;
        else if ((e3 > e1 && e3 < e2)||(e3 < e1 && e3 > e2))
          return TACAgent.TYPE_MUSEUM;
      case 3:
        if(e1 < e2 && e1 < e3)
          return TACAgent.TYPE_ALLIGATOR_WRESTLING;
        else if (e2 < e1 && e2 < e3)
          return TACAgent.TYPE_AMUSEMENT;
        else if (e3 < e1 && e3 < e2)
          return TACAgent.TYPE_MUSEUM;
      default:
        return -1;
      }
      
  }



  // -------------------------------------------------------------------
  // Only for backward compability
  // -------------------------------------------------------------------

  public static void main (String[] args) {
    TACAgent.main(args);
  }

} // DummyAgent
