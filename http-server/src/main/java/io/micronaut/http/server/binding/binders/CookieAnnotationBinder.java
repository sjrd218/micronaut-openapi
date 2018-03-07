/*
 * Copyright 2017 original authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package io.micronaut.http.server.binding.binders;

import io.micronaut.http.annotation.CookieValue;
import io.micronaut.core.bind.annotation.AbstractAnnotatedArgumentBinder;
import io.micronaut.core.bind.annotation.AnnotatedArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.CookieValue;
import io.micronaut.core.type.Argument;

/**
 * An {@link AnnotatedArgumentBinder} implementation that uses the {@link CookieValue} annotation
 * to trigger binding from an HTTP {@link io.micronaut.http.cookie.Cookie}
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public class CookieAnnotationBinder<T> extends AbstractAnnotatedArgumentBinder<CookieValue, T, HttpRequest<?>> implements AnnotatedRequestArgumentBinder<CookieValue, T> {
    public CookieAnnotationBinder(ConversionService<?> conversionService) {
        super(conversionService);
    }

    @Override
    public Class<CookieValue> getAnnotationType() {
        return CookieValue.class;
    }

    @Override
    public BindingResult<T> bind(ArgumentConversionContext<T> argument, HttpRequest<?> source) {
        ConvertibleValues<io.micronaut.http.cookie.Cookie> parameters = source.getCookies();
        CookieValue annotation = argument.getAnnotation(CookieValue.class);
        String parameterName = annotation.value();
        return doBind(argument, parameters, parameterName);
    }

    @Override
    protected String getFallbackFormat(Argument argument) {
        return NameUtils.hyphenate(argument.getName());
    }
}