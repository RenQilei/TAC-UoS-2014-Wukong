/**
 * SICS TAC Server
 * http://www.sics.se/tac/    tac-dev@sics.se
 *
 * Copyright (c) 2001, 2002 SICS AB. All rights reserved.
 * -----------------------------------------------------------------
 *
 * Bid
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Jul 07 13:35:35 2004
 * Updated : $Date: 2004/07/07 12:58:41 $
 *           $Revision: 1.1 $
 */
package se.sics.tac.util;
import com.botbox.util.ArrayUtils;

public class Bid {

  public static final char SUBMISSION = 's';
  public static final char REPLACEMENT = 'r';
  public static final char TRANSACTION = 't';
  public static final char WITHDRAWAL = 'w';
  public static final char REJECTED = 'c';

  private int bidID;
  private int agent;
  private int auction;
  private char bidType;
  private int processStatus;

  private int[] bidQuantity = new int[2];
  private float[] bidPrice = new float[2];
  private int bidCount = 0;

  public Bid(int bidID, int agent, int auction, String bidType,
	     int processStatus) {
    this.bidID = bidID;
    this.agent = agent;
    this.auction = auction;
    this.processStatus = processStatus;
    if (bidType.length() != 1) {
      throw new IllegalArgumentException("illegal bid type: '" + bidType
					 + '\'');
    }
    this.bidType = bidType.charAt(0);
  }

  public int getBidID() {
    return bidID;
  }

  public int getAgent() {
    return agent;
  }

  public int getAuction() {
    return auction;
  }

  public char getBidType() {
    return bidType;
  }

  public int getProcessStatus() {
    return processStatus;
  }

  public void addBidPoint(int quantity, float price) {
    if (bidCount == bidQuantity.length) {
      bidQuantity = ArrayUtils.setSize(bidQuantity, bidCount + 5);
      bidPrice = ArrayUtils.setSize(bidPrice, bidCount + 5);
    }
    bidQuantity[bidCount] = quantity;
    bidPrice[bidCount] = price;
    bidCount++;
  }

  public int getBidQuantity(int index) {
    if (index >= bidCount) {
      throw new IndexOutOfBoundsException();
    }
    return bidQuantity[index];
  }

  public float getBidPrice(int index) {
    if (index >= bidCount) {
      throw new IndexOutOfBoundsException();
    }
    return bidPrice[index];
  }

  public int getBidCount() {
    return bidCount;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Bid[").append(bidID)
      .append(',').append(agent)
      .append(',').append(auction)
      .append(',').append(bidType)
      .append(',').append(processStatus)
      .append(",[");
    for (int i = 0, n = bidCount; i < n; i++) {
      if (i > 0) {
	sb.append(',');
      }
      sb.append(bidQuantity[i]).append(',').append(bidPrice[i]);
    }
    return sb.append("]]").toString();
  }

} // Bid
