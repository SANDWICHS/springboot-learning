package cn.zwq.cat.context;

import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.Cat.Context;

public class CatContext implements Context {

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
		appendProperty(sb, Context.ROOT);
		appendProperty(sb, Context.PARENT);
		appendProperty(sb, Context.CHILD);
		return sb.toString();
	}

	public static CatContext deserialize(String value) {
		CatContext context = new CatContext();
		String[] values = value.split(SEPARATOR);
		context.setProperty(Context.ROOT, values[0]);
		context.setProperty(Context.PARENT, values[1]);
		context.setProperty(Context.CHILD, values[2]);
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
}
