/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brixcms.web.util;

import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.encoding.UrlDecoder;
import org.apache.wicket.util.string.PrependingStringBuffer;
import org.brixcms.Path;
import org.brixcms.jcr.wrapper.BrixNode;
import org.brixcms.web.generic.BrixGenericWebMarkupContainer;

public abstract class PathLabel extends BrixGenericWebMarkupContainer<BrixNode> {
    private final String rootPath;
    private final Behavior requestListener;

    public PathLabel(String id, IModel<BrixNode> model, String rootPath) {
        super(id, model);
        this.rootPath = rootPath;
        add(requestListener = new AbstractAjaxBehavior(){
            @Override
            public void onRequest() {
                String path = getRequest().getRequestParameters().getParameterValue("path").toString();
                // if (path == null) {
                // path = getRequestCycle().getPageParameters().getString("path");
                // }
                path = UrlDecoder.QUERY_INSTANCE.decode(path, getRequest().getCharset());
                onPathClicked(new Path(path));
            }
        });
    }

    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        PrependingStringBuffer b = new PrependingStringBuffer();
        BrixNode current = getModelObject();

        while (true) {
            StringBuilder builder = new StringBuilder();
            writePath(current, builder, current.equals(getModelObject()));
            // bootstrap does this
            // if (b.length() > 0) {
            // b.prepend("&nbsp;/&nbsp;");
            // }
            b.prepend(builder.toString());
            if (current.getDepth() == 0 || current.getPath().equals(rootPath)) {
                break;
            }
            current = (BrixNode) current.getParent();
        }

        final Response r = getResponse();
        r.write(b.toString());
    }

    private void writePath(BrixNode node, StringBuilder builder, boolean last) {
        if (last) {
            builder.append("<span class=\"breadcrumb-item active\">");
            builder.append(node.getUserVisibleName());
            builder.append("</span>");
        }else{
            builder.append("<a class=\"breadcrumb-item\" href=\"");
            builder.append(createCallbackUrl(node.getPath()));
            builder.append("\">");
            builder.append(node.getUserVisibleName());
            builder.append("</a>");
        }
    }

    private CharSequence createCallbackUrl(String subpath) {
        Url url = Url.parse(urlForListener(requestListener, null).toString());
        url.addQueryParameter("path", subpath);
        return url.toString(getRequest().getCharset());
    }

    protected abstract void onPathClicked(Path path);
}
