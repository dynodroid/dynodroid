package edu.gatech.dynodroid.hierarchyHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


//Copied from hierarchyviewer
public class ViewNode {
	public static final String MISCELLANIOUS = "miscellaneous";
	public String id;
	public String name;
	public String hashCode;
	public List<Property> properties = new ArrayList<Property>();

	public Map<String, Property> namedProperties = new HashMap<String, Property>();
	public ViewNode parent;
	public List<ViewNode> children = new ArrayList<ViewNode>();
	public int left;
	public int top;
	public int width;
	public int height;
	public int scrollX;
	public int scrollY;
	public int paddingLeft;
	public int paddingRight;
	public int paddingTop;
	public int paddingBottom;
	public int realLeft = 0;
	public int realTop = 0;
	public int marginLeft;
	public int marginRight;
	public int marginTop;
	public boolean isClickable;
	public int marginBottom;
	public int baseline;
	public boolean willNotDraw;
	public boolean hasMargins;
	public boolean hasFocus;
	public int index;
	public double measureTime;
	public double layoutTime;
	public float appScale = 1.0f;
	public double drawTime;
	public boolean canAcceptGestures = false;
	public String callBackName;
	public int uniqueViewID=-1;
	public boolean m3Clickable;
	public boolean isEnabled = true;
	public boolean isLongClickable;
	public boolean needManualInteraction=false;
	public ProfileRating measureRating = ProfileRating.NONE;
	public String textBoxText = null;

	public ProfileRating layoutRating = ProfileRating.NONE;

	public ProfileRating drawRating = ProfileRating.NONE;

	public Set<String> categories = new TreeSet<String>();
	public Window window;
	public int imageReferences = 1;
	public int viewCount;
	public boolean filtered;
	public int protocolVersion;
	public boolean onClickCallBackFound;
	public static String menuWidgetString = "menuKey@com.machiry.bitdroid";
	public static final String backWidgetString = "backKey@com.machiry.bitdroid";

	public ViewNode(Window paramWindow, ViewNode paramViewNode,
			String paramString) {
		this.window = paramWindow;
		this.parent = paramViewNode;
		this.index = (this.parent == null ? 0 : this.parent.children.size());
		if (this.parent != null) {
			this.parent.children.add(this);
			this.realLeft = this.parent.realLeft;
			this.realTop = this.parent.realTop;
		}
		int i = paramString.indexOf('@');
		this.name = paramString.substring(0, i);
		paramString = paramString.substring(i + 1);
		i = paramString.indexOf(' ');
		this.hashCode = paramString.substring(0, i);
		loadProperties(paramString.substring(i + 1).trim());
		this.measureTime = -1.0D;
		this.layoutTime = -1.0D;
		this.drawTime = -1.0D;
	}

	public ViewNode(Window paramWindow, String id, boolean isMenu) {
		this.window = paramWindow;
		this.id = id;
		this.hashCode = "com.machiry.bitdroid";
		if (isMenu) {
			this.name = "menuKey";
		} else {
			this.name = "backKey";

		}
	}

	public void dispose() {
		int i = this.children.size();
		for (int j = 0; j < i; j++) {
			this.children.get(j).dispose();
		}
		dereferenceImage();
	}

	public void referenceImage() {
		this.imageReferences += 1;
	}

	public void dereferenceImage() {
		this.imageReferences -= 1;
	}

	private void loadProperties(String paramString) {
		int i = 0;
		Object localObject;
		int m;
		int j;
		do {
			int k = paramString.indexOf('=', i);
			localObject = new Property();
			((Property) localObject).name = paramString.substring(i, k);

			m = paramString.indexOf(',', k + 1);
			int n = Integer.parseInt(paramString.substring(k + 1, m));
			i = m + 1 + n;
			((Property) localObject).value = paramString.substring(m + 1, m + 1
					+ n);

			this.properties.add((Property) localObject);
			this.namedProperties.put(((Property) localObject).name,
					(Property) localObject);

			j = i >= paramString.length() ? 1 : 0;
			if (j == 0)
				i++;
		} while (j == 0);

		Collections.sort(this.properties, new Comparator<Property>() {
			@Override
			public int compare(Property arg0, Property arg1) {
				if (arg0 == null) {
					return arg1 == null ? 0 : -1;
				}
				if (arg1 == null) {
					return arg0 == null ? 0 : 1;
				}
				return arg0.name.compareTo(arg1.name);
			}
		});
		this.id = this.namedProperties.get("mID").value;
		this.isEnabled = getBoolean("isEnabled()", true);
		
		if(this.namedProperties.containsKey("M3:m3IsTextEditor()")){
			if(getBoolean("M3:m3IsTextEditor()",false)){
				this.textBoxText = "dummyText";
			}
		}

		this.left = (this.namedProperties.containsKey("mLeft") ? getInt(
				"mLeft", 0) : getInt("layout:mLeft", 0));
		

		this.top = (this.namedProperties.containsKey("mTop") ? getInt("mTop", 0)
				: getInt("layout:mTop", 0));

		this.width = (this.namedProperties.containsKey("getWidth()") ? getInt(
				"getWidth()", 0) : getInt("layout:getWidth()", 0));

		this.height = (this.namedProperties.containsKey("getHeight()") ? getInt(
				"getHeight()", 0) : getInt("layout:getHeight()", 0));

		this.scrollX = (this.namedProperties.containsKey("mScrollX") ? getInt(
				"mScrollX", 0) : getInt("scrolling:mScrollX", 0));

		this.scrollY = (this.namedProperties.containsKey("mScrollY") ? getInt(
				"mScrollY", 0) : getInt("scrolling:mScrollY", 0));

		this.paddingLeft = (this.namedProperties.containsKey("mPaddingLeft") ? getInt(
				"mPaddingLeft", 0) : getInt("padding:mPaddingLeft", 0));

		this.paddingRight = (this.namedProperties.containsKey("mPaddingRight") ? getInt(
				"mPaddingRight", 0) : getInt("padding:mPaddingRight", 0));

		this.paddingTop = (this.namedProperties.containsKey("mPaddingTop") ? getInt(
				"mPaddingTop", 0) : getInt("padding:mPaddingTop", 0));

		this.paddingBottom = (this.namedProperties
				.containsKey("mPaddingBottom") ? getInt("mPaddingBottom", 0)
				: getInt("padding:mPaddingBottom", 0));
		this.uniqueViewID = getInt("M3:m3ID()",-1);

		this.marginLeft = (this.namedProperties
				.containsKey("layout_leftMargin") ? getInt("layout_leftMargin",
				-2147483648) : getInt("layout:layout_leftMargin", -2147483648));

		this.marginRight = (this.namedProperties
				.containsKey("layout_rightMargin") ? getInt(
				"layout_rightMargin", -2147483648) : getInt(
				"layout:layout_rightMargin", -2147483648));

		this.marginTop = (this.namedProperties.containsKey("layout_topMargin") ? getInt(
				"layout_topMargin", -2147483648) : getInt(
				"layout:layout_topMargin", -2147483648));

		this.marginBottom = (this.namedProperties
				.containsKey("layout_bottomMargin") ? getInt(
				"layout_bottomMargin", -2147483648) : getInt(
				"layout:layout_bottomMargin", -2147483648));

		this.baseline = (this.namedProperties.containsKey("getBaseline()") ? getInt(
				"getBaseline()", 0) : getInt("layout:getBaseline()", 0));

		this.willNotDraw = (this.namedProperties.containsKey("willNotDraw()") ? getBoolean(
				"willNotDraw()", false) : getBoolean("drawing:willNotDraw()",
				false));

		this.isClickable = (this.namedProperties.containsKey("isClickable()") ? getBoolean(
				"isClickable()", false) : getBoolean("layout:isClickable()",
				false));

		this.hasFocus = (this.namedProperties.containsKey("hasFocus()") ? getBoolean(
				"hasFocus()", false) : getBoolean("focus:hasFocus()", false));

		this.hasMargins = ((this.marginLeft != -2147483648)
				&& (this.marginRight != -2147483648)
				&& (this.marginTop != -2147483648) && (this.marginBottom != -2147483648));
		
		this.appScale = getFloat("M3:m3getAppScale()", 1.0f);
		   this.realLeft = getInt("M3:getXM3()", 0);
		   this.realTop = getInt("M3:getYM3()", 0);
		   this.onClickCallBackFound = getBoolean("M3:hasOnClickRegistered()", false);
		    this.callBackName = (this.namedProperties.containsKey("M3:getCallBackClass()")?((Property)this.namedProperties.get("M3:getCallBackClass()")).value:"");
		    this.m3Clickable = this.isClickable;
		    if(parent != null){
		    	this.m3Clickable = this.m3Clickable || this.parent.m3Clickable;
		    }
		    
		    if(!this.m3Clickable && this.namedProperties.get("M3:m3ListItemClickListner()") != null && this.namedProperties.get("M3:m3ListItemClickListner()").value != null){
		    	if(!this.namedProperties.get("M3:m3ListItemClickListner()").value.equals("NULL")){
		    		this.m3Clickable = true;
		    	}
		    }
		    
		    this.isLongClickable = getBoolean("M3:hasOnLongClickRegistered()", false); 
		    
		    this.canAcceptGestures = getBoolean("M3:canAcceptSlideInput()", false); 
		    
		    this.needManualInteraction = getBoolean("M3:IsWebView()", false); 
		    
		    if(parent != null){
		    	this.isLongClickable = this.isLongClickable || this.parent.isLongClickable;
		    }
		    if(!this.isLongClickable && this.namedProperties.get("M3:m3ListItemLongClickListner()") != null && this.namedProperties.get("M3:m3ListItemLongClickListner()").value != null) {
		    	if(!this.namedProperties.get("M3:m3ListItemLongClickListner()").value.equals("NULL")){
		    		this.isLongClickable = true;
		    	}
		    }
		    

		for (Iterator<String> localIterator = this.namedProperties.keySet()
				.iterator(); localIterator.hasNext();) {
			localObject = localIterator.next();
			m = ((String) localObject).indexOf(':');
			if (m != -1) {
				this.categories.add(((String) localObject).substring(0, m));
			}
		}
		if (this.categories.size() != 0)
			this.categories.add("miscellaneous");
	}

	public void setProfileRatings() {
		int i = this.children.size();
		if (i > 1) {
			double d1 = 0.0D;
			double d2 = 0.0D;
			double d3 = 0.0D;
			ViewNode localViewNode;
			for (int k = 0; k < i; k++) {
				localViewNode = this.children.get(k);
				d1 += localViewNode.measureTime;
				d2 += localViewNode.layoutTime;
				d3 += localViewNode.drawTime;
			}
			for (int k = 0; k < i; k++) {
				localViewNode = this.children.get(k);
				if (localViewNode.measureTime / d1 >= 0.8D)
					localViewNode.measureRating = ProfileRating.RED;
				else if (localViewNode.measureTime / d1 >= 0.5D)
					localViewNode.measureRating = ProfileRating.YELLOW;
				else {
					localViewNode.measureRating = ProfileRating.GREEN;
				}
				if (localViewNode.layoutTime / d2 >= 0.8D)
					localViewNode.layoutRating = ProfileRating.RED;
				else if (localViewNode.layoutTime / d2 >= 0.5D)
					localViewNode.layoutRating = ProfileRating.YELLOW;
				else {
					localViewNode.layoutRating = ProfileRating.GREEN;
				}
				if (localViewNode.drawTime / d3 >= 0.8D)
					localViewNode.drawRating = ProfileRating.RED;
				else if (localViewNode.drawTime / d3 >= 0.5D)
					localViewNode.drawRating = ProfileRating.YELLOW;
				else {
					localViewNode.drawRating = ProfileRating.GREEN;
				}
			}
		}
		for (int j = 0; j < i; j++)
			this.children.get(j).setProfileRatings();
	}

	public void setViewCount() {
		this.viewCount = 1;
		int i = this.children.size();
		for (int j = 0; j < i; j++) {
			ViewNode localViewNode = this.children.get(j);
			localViewNode.setViewCount();
			this.viewCount += localViewNode.viewCount;
		}
	}

	public void filter(String paramString) {
		int i = this.name.lastIndexOf('.');
		String str = i == -1 ? this.name : this.name.substring(i + 1);
		this.filtered = ((!paramString.equals("")) && ((str.toLowerCase()
				.contains(paramString.toLowerCase())) || ((!this.id
				.equals("NO_ID")) && (this.id.toLowerCase()
				.contains(paramString.toLowerCase())))));

		int j = this.children.size();
		for (int k = 0; k < j; k++)
			this.children.get(k).filter(paramString);
	}

	private boolean getBoolean(String paramString, boolean paramBoolean) {
		Property localProperty = this.namedProperties
				.get(paramString);
		if (localProperty != null) {
			try {
				return Boolean.parseBoolean(localProperty.value);
			} catch (NumberFormatException localNumberFormatException) {
				return paramBoolean;
			}
		}
		return paramBoolean;
	}

	private int getInt(String paramString, int paramInt) {
		Property localProperty = this.namedProperties
				.get(paramString);
		if (localProperty != null) {
			try {
				return Integer.parseInt(localProperty.value);
			} catch (NumberFormatException localNumberFormatException) {
				return paramInt;
			}
		}
		return paramInt;
	} 
	private float getFloat(String paramString, float paramfloat) {
		Property localProperty = this.namedProperties
				.get(paramString);
		if (localProperty != null) {
			try {
				return Float.parseFloat(localProperty.value);
			} catch (NumberFormatException localNumberFormatException) {
				return paramfloat;
			}
		}
		return paramfloat;
	} 

	@Override
	public String toString() {
		return this.name + "@" + this.hashCode;
	}

	@Override
	public int hashCode() {
		return this.hashCode.hashCode();
	}

	public boolean hasSameSize(ViewNode node) {
		if (node != null) {
			return this.width == node.width && this.height == node.height
					&& this.realLeft == node.realLeft
					&& this.realTop == node.realTop;
		}
		return false;
	}

	public int sizeHash() {
		return this.width ^ this.height ^ this.realLeft ^ this.realTop;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && (obj instanceof ViewNode)) {
			ViewNode that = (ViewNode) obj;
			return that.name.equals(this.name) && that.id.equals(this.id);
		}
		return false;
	}

	public static class Property {
		public String name;
		public String value;

		@Override
		public String toString() {
			return this.name + '=' + this.value;
		}
	}

	public static enum ProfileRating {
		RED, YELLOW, GREEN, NONE;
	}
}