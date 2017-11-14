package edu.gatech.dynodroid.hierarchyHelper;

public enum ViewElementAction {
	OnClick,
	OnLongClick,
	BackButton,
	MenuButton,
	OnSlide;
	
	public static ViewElementAction fromString(String eventName){
		assert((eventName != null) && eventName.length() > 0);
		if(eventName.equals("onClick")){
			return OnClick;
		}
		if(eventName.equals("onLongClick")){
			return OnLongClick;
		}
		if(eventName.equals("onSlide")){
			return OnSlide;
		}
		if(eventName.equals("BackButton")){
			return BackButton;
		}
		if(eventName.equals("MenuButton")){
			return MenuButton;
		}
		return null;
	}
};
