package server;

import java.security.PublicKey;

import javax.swing.plaf.synth.SynthOptionPaneUI;

import crypto.RSAProvider;
import shared.Point2D;
import shared.TrackerLocationSystem;

public class ProofReport {

	private int proverID;
	private int witnessID;
	private Point2D proverPoint;
	private Point2D witnessPoint;
	private int epoch;
	private String WitnessDigSig;
	private String ProverDigSig;
	private boolean witnessIsNearProof;
	
	public ProofReport(int proverID, int witnessID, Point2D proverPoint, Point2D witnessPoint, int epoch, Boolean witnessIsNearProof,
			String digSig1, String digSig2) {
		super();
		this.proverID = proverID;
		this.witnessID = witnessID;
		this.proverPoint = proverPoint;
		this.witnessPoint = witnessPoint;
		this.epoch = epoch;
		this.witnessIsNearProof = witnessIsNearProof;
		this.WitnessDigSig = digSig2;
		this.ProverDigSig = digSig1;
	}
	
	public ProofReport(String reportString) {
		init(reportString);
	}

	private void init(String reportString) {
		if(reportString.charAt(0) ==' ')
			reportString= reportString.replaceFirst(" ", "");
		String[] splitReport = reportString.split(" ");
		this.proverID = Integer.parseInt(splitReport[0]);
		this.witnessID = Integer.parseInt(splitReport[1]);
		String ProverpointString = splitReport[2];
		String witnesspointString = splitReport[3];
		this.proverPoint = getPointFromString(ProverpointString);
		this.witnessPoint = getPointFromString(witnesspointString);
		this.epoch = Integer.parseInt(splitReport[4]);
		this.witnessIsNearProof = Boolean.parseBoolean(splitReport[5]);
		this.WitnessDigSig = splitReport[6];
	}

	private Point2D getPointFromString(String pointString) {
		pointString = pointString.replace("(", "").replace(")", "");
		String[] pts = pointString.split(";");
		int X = Integer.parseInt(pts[0]);
		int Y = Integer.parseInt(pts[1]);
		Point2D p = new Point2D(X, Y);
		return p;
	}

	public int getProverID() {
		return proverID;
	}

	public int getWitnessID() {
		return witnessID;
	}

	public Point2D getProverPoint() {
		return proverPoint;
	}

	public Point2D getWitnessPoint() {
		return witnessPoint;
	}

	public int getEpoch() {
		return epoch;
	}

	public String getWitnessDigSig() {
		return WitnessDigSig;
	}
	
	public String getPorverDigSig() {
		return ProverDigSig;
	}

	public boolean isWitnessIsNearProof() {
		return witnessIsNearProof;
	}
	
	public boolean isfakereport(int requestID) {
        return this.proverID != requestID;
    }
	
	@Override
	public String toString() {
		return proverID +" "+ witnessID +" "+ proverPoint.toString() +" "+ witnessPoint.toString() +" "+ epoch +" "+ witnessIsNearProof;
	}
	
	public boolean proofDigSigIsValid(PublicKey witPubKey) throws Exception {
		String proof = this.toString();
		return RSAProvider.istextAuthentic(proof, this.WitnessDigSig, witPubKey);
	}
	
	
}
