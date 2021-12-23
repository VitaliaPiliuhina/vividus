/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.browserup.harreader.model.Har;
import com.browserup.harreader.model.HarCreatorBrowser;
import com.browserup.harreader.model.HarEntry;
import com.browserup.harreader.model.HarLog;
import com.browserup.harreader.model.HarPostData;
import com.browserup.harreader.model.HarPostDataParam;
import com.browserup.harreader.model.HarQueryParam;
import com.browserup.harreader.model.HarRequest;
import com.browserup.harreader.model.HarResponse;
import com.browserup.harreader.model.HttpMethod;
import com.google.common.eventbus.EventBus;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.proxy.IProxy;

@ExtendWith(MockitoExtension.class)
class GenericHarOnFailureMonitorTests
{
    private static final String URL = "www.test.com";
    private static final String KEY1 = "key1";
    private static final String VALUE1 = "value1";
    private static final String KEY2 = "key2";
    private static final String VALUE2 = "value2";
    private static final String MIME_TYPE = "mimeType";
    private static final String TEXT = "text";

    @Mock private EventBus eventBus;
    @Mock private IProxy proxy;
    @InjectMocks private GenericHarOnFailureMonitor monitor;

    @Test
    void shouldTakeAssertionFailureHar()
    {
        monitor.setProxy(proxy);
        Har harMock = mockHar();

        Optional<Har> har = monitor.takeAssertionFailureHar();

        assertTrue(har.isPresent());
        assertEquals(harMock, har.get());
        verifyNoMoreInteractions(eventBus);
    }

    @Test
    void shouldTakeHarWithUniqueEntries()
    {
        monitor.setProxy(proxy);
        mockHar();
        List<HarEntry> entries = new ArrayList<>();
        entries.add(createHarEntry());
        entries.add(createHarEntry());
        monitor.setHarEntries(entries);

        Optional<Har> har = monitor.takeAssertionFailureHar();

        assertTrue(har.isPresent());
        verifyNoMoreInteractions(eventBus);
    }

    @Test
    void shouldTakeHarWithSimilarCountOfEntries()
    {
        monitor.setProxy(proxy);
        mockHar();
        List<HarEntry> entries = new ArrayList<>();
        entries.add(createHarEntry());
        monitor.setHarEntries(entries);

        Optional<Har> har = monitor.takeAssertionFailureHar();

        assertTrue(har.isPresent());
        verifyNoMoreInteractions(eventBus);
    }

    private Har mockHar()
    {
        HarEntry harEntry = createHarEntry();

        HarCreatorBrowser browser = new HarCreatorBrowser();
        browser.setName("chrome");
        browser.setVersion("66");

        HarLog harLog = new HarLog();
        harLog.setBrowser(browser);
        harLog.setCreator(browser);
        harLog.setEntries(List.of(harEntry));

        Har har = new Har();
        har.setLog(harLog);
        when(proxy.getRecordedData()).thenReturn(har);
        return har;
    }

    private HarEntry createHarEntry()
    {
        HarPostData postData = new HarPostData();
        postData.setMimeType(MIME_TYPE);
        postData.setText(TEXT);
        postData.setParams(List.of(
                createHarPostDataParam(KEY1, VALUE1),
                createHarPostDataParam(KEY1, VALUE2),
                createHarPostDataParam(KEY2, VALUE2)
        ));

        HarRequest request = new HarRequest();
        request.setMethod(HttpMethod.POST);
        request.setUrl(URL);
        request.setQueryString(List.of(
                createHarQueryParam(KEY1, VALUE1),
                createHarQueryParam(KEY1, VALUE2),
                createHarQueryParam(KEY2, VALUE2)
        ));
        request.setPostData(postData);

        HarResponse response = new HarResponse();
        response.setStatus(HttpStatus.SC_OK);

        HarEntry harEntry = new HarEntry();
        harEntry.setRequest(request);
        harEntry.setResponse(response);
        return harEntry;
    }

    private HarQueryParam createHarQueryParam(String key, String value)
    {
        HarQueryParam harQueryParam = new HarQueryParam();
        harQueryParam.setName(key);
        harQueryParam.setValue(value);
        return harQueryParam;
    }

    private HarPostDataParam createHarPostDataParam(String key, String value)
    {
        HarPostDataParam postDataParam = new HarPostDataParam();
        postDataParam.setName(key);
        postDataParam.setValue(value);
        return postDataParam;
    }
}
