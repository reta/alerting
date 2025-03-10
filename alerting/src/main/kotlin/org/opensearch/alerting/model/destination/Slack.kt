/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package org.opensearch.alerting.model.destination

import org.opensearch.alerting.elasticapi.string
import org.opensearch.common.Strings
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils.ensureExpectedToken
import org.opensearch.common.xcontent.XContentType
import java.io.IOException
import java.lang.IllegalStateException

/**
 * A value object that represents a Slack message. Slack message will be
 * submitted to the Slack destination
 */
data class Slack(val url: String) : ToXContent {

    init {
        require(!Strings.isNullOrEmpty(url)) { "URL is null or empty" }
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject(TYPE)
            .field(URL, url)
            .endObject()
    }

    @Throws(IOException::class)
    fun writeTo(out: StreamOutput) {
        out.writeString(url)
    }

    companion object {
        const val URL = "url"
        const val TYPE = "slack"

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): Slack {
            lateinit var url: String

            ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()
                when (fieldName) {
                    URL -> url = xcp.text()
                    else -> {
                        throw IllegalStateException("Unexpected field: $fieldName, while parsing Slack destination")
                    }
                }
            }
            return Slack(url)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): Slack? {
            return if (sin.readBoolean()) {
                Slack(sin.readString())
            } else null
        }
    }

    fun constructMessageContent(subject: String?, message: String): String {
        val messageContent: String? = if (Strings.isNullOrEmpty(subject)) message else "$subject \n\n $message"
        val builder = XContentFactory.contentBuilder(XContentType.JSON)
        builder.startObject()
            .field("text", messageContent)
            .endObject()
        return builder.string()
    }
}
