/**
 * 
 */
package edu.gatech.dynodroid.rmiRequest;

/**
 * @author machiry
 *
 */
public enum ServerRequestStatus {
	RECEIVED {
		@Override
		public String toString(){
			return "The Request has been received and will be scheduled";
		}
	},
	EMULATOR_BUSY {
		@Override
		public String toString(){
			return "The is no free emulator available to schedule this request";
		}
	},
	EMULATOR_CREATED {
		@Override
		public String toString(){
			return "Emulator has been created on request and will be tested";
		}
	},
	TEST_SCHEDULED {
		@Override
		public String toString(){
			return "Testing has been scheduled";
		}
	},
	COMPLETED {
		@Override
		public String toString(){
			return "The Processing of the Request Completed";
		}
	},
	PROBLEM_OCCURED {
		@Override
		public String toString(){
			return "Problem occured while processing the request";
		}
	},
	INPUT_INVALID {
		@Override
		public String toString(){
			return "Provided Input is not valid";
		}
	}
}
