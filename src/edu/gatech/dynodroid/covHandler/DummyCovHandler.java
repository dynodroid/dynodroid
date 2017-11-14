package edu.gatech.dynodroid.covHandler;

public class DummyCovHandler extends CoverageHandler{

	@Override
	public String computeCoverageReport(String coverageFile, String type) {
		// TODO Auto-generated method stub
		return "DUMMY";
	}

	@Override
	public String computeCoverageReport(String coverageFile, String type,
			String srcPath) {
		// TODO Auto-generated method stub
		return "DUMMY";
	}

	@Override
	public boolean setReportDir(String targetDir) {
		// TODO Auto-generated method stub
		return true;
	}

}
