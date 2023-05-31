package cn.zwq.cat;

import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.Cat;
import com.dianping.cat.Cat.Context;

public class CatContext implements Cat.Context {

	private Map<String, String> properties = new HashMap<>();

	private static final String SEPARATOR = "__@s@e@P@__";
	private static final String PLACEHOLDER = "__null__";

	public CatContext() {
		super();
	}

	public CatContext(String childId) {
		addProperty(Context.CHILD, childId);
	}

	public CatContext(String childId, String rootId) {
		this(childId);
		addProperty(Context.ROOT, rootId);
	}

	public CatContext(String childId, String rootId, String parentId) {
		this(childId, rootId);
		addProperty(Context.PARENT, parentId);
	}

	@Override
	public void addProperty(String key, String value) {
		properties.put(key, value);
	}

	@Override
	public String getProperty(String key) {
		return properties.get(key);
	}

	// ROOT*#06#PARENT*#06#CHILD
	public String serialize() {
		StringBuilder sb = new StringBuilder();
		appendProperty(sb, Cat.Context.ROOT);
		appendProperty(sb, Cat.Context.PARENT);
		appendProperty(sb, Cat.Context.CHILD);
		return sb.toString();
	}

	public static CatContext deserialize(String value) {
		CatContext context = new CatContext();
		String[] values = value.split(SEPARATOR);
		context.setProperty(Cat.Context.ROOT, values[0]);
		context.setProperty(Cat.Context.PARENT, values[1]);
		context.setProperty(Cat.Context.CHILD, values[2]);
		return context;
	}

	private void setProperty(String key, String value) {
		if (PLACEHOLDER.equals(value)) {
			this.addProperty(key, null);
		} else {
			this.addProperty(key, value);
		}
	}

	private void appendProperty(StringBuilder sb, String key) {
		String root = getProperty(key);
		if (root != null && root.length() > 0) {
			sb.append(root).append(SEPARATOR);
		} else {
			sb.append(PLACEHOLDER).append(SEPARATOR);
		}
	}

	public CatContext copy() {
		return new CatContext(this.getProperty(Context.CHILD), this.getProperty(Context.ROOT),
				this.getProperty(Context.PARENT));
	}

	@Override
	public String toString() {
		return String.format("CatContext [ROOT:%s,PARENT:%s,CHILD:%s]", this.getProperty(Context.ROOT), this.getProperty(Context.PARENT),
				this.getProperty(Context.CHILD));
	}

}
