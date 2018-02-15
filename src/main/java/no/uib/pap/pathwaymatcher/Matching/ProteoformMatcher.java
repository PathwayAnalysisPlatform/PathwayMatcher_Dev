package no.uib.pap.pathwaymatcher.Matching;

import no.uib.pap.model.Proteoform;
import no.uib.pap.pathwaymatcher.PathwayMatcher14;

public abstract class ProteoformMatcher {

	public abstract Boolean matches(Proteoform iP, Proteoform rP);

	public boolean matches(Long iC, Long rC) {
		if (iC != null) {
			if (iC == -1L)
				iC = null;
		}
		if (rC != null) {
			if (rC == -1L)
				rC = null;
		}
		if (iC != null && rC != null) {
			if (iC != rC) {
				if (Math.abs(iC - rC) > PathwayMatcher14.margin) {
					return false;
				}
			}
		}
		return true;
	}
}
