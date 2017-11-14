package edu.gatech.dynodroid.rmiRequest;

public enum ServerRequestType {
	APK {
		@Override
		public String toString(){
			return "The Request has been received with apk";
		}
	},
	SOURCES {
		@Override
		public String toString(){
			return "This request has been received with sources";
		}
	}
}
