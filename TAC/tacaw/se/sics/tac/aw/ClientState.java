package se.sics.tac.aw;

public class ClientState {
	int inFlight;
	int outFlight;
	int hotel;
	int value;
	
	public ClientState(int inFlight, int outFlight, int hotel, int[] preference) {
		this.inFlight = inFlight;
		this.outFlight = outFlight;
		this.hotel = hotel;
		
		this.value = evaluation(inFlight, outFlight, hotel, preference);
	}
	
	private int evaluation(int in, int out, int hp, int[] preference) {
		
		return 1000-100*(Math.abs(preference[0]-in)+Math.abs(preference[1]-out))+(hp>50?hp:0);
	}
}
