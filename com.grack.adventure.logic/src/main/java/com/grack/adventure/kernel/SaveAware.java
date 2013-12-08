package com.grack.adventure.kernel;

import java.util.List;

/**
 * Save an object to a list of {@link String}s. This would make far more sense
 * to do as JSON.
 */
public interface SaveAware {
	public List<String> save();
}
