/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.playwright;

import static org.junitpioneer.playwright.PlaywrightUtils.PLAYWRIGHT_NAMESPACE;
import static org.junitpioneer.playwright.PlaywrightUtils.closeResourceLater;
import static org.junitpioneer.playwright.PlaywrightUtils.isPlaywrightExtensionActive;

import java.util.Optional;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junitpioneer.internal.Lazy;

public class BrowserContextParameterResolver implements ParameterResolver {

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return isPlaywrightExtensionActive(extensionContext)
				&& parameterContext.getParameter().getType() == BrowserContext.class;
	}

	@Override
	public BrowserContext resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return createBrowserContext(extensionContext,
			Lazy.from(() -> AnnotationUtils.findAnnotation(extensionContext.getElement(), PlaywrightTest.class)));
	}

	static BrowserContext createBrowserContext(ExtensionContext extensionContext,
			Lazy<Optional<PlaywrightTest>> configuration) {
		// @formatter:off
		return extensionContext
			.getStore(PLAYWRIGHT_NAMESPACE)
			.getOrComputeIfAbsent(
				"browserContext",
				__ -> {
					Browser browser = BrowserParameterResolver.createBrowser(extensionContext, configuration);
					BrowserContext browserContext = browser.newContext();
					closeResourceLater(extensionContext, browserContext::close);
					return browserContext;
				},
				BrowserContext.class);
		// @formatter:on
	}

}