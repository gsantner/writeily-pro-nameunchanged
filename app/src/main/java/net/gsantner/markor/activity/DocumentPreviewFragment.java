/*
 * Copyright (c) 2017 Gregor Santner and Markor contributors
 *
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.gsantner.markor.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.gsantner.markor.R;
import net.gsantner.markor.format.converter.MarkdownConverter;
import net.gsantner.markor.model.Document;
import net.gsantner.markor.ui.BaseFragment;
import net.gsantner.markor.util.ContextUtils;
import net.gsantner.markor.util.DocumentIO;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DocumentPreviewFragment extends BaseFragment {
    public static boolean showEditOnBack = false;
    public static final String FRAGMENT_TAG = "DocumentPreviewFragment";

    public static DocumentPreviewFragment newInstance(Document document) {
        DocumentPreviewFragment f = new DocumentPreviewFragment();
        Bundle args = new Bundle();
        args.putSerializable(DocumentIO.EXTRA_DOCUMENT, document);
        f.setArguments(args);
        return f;
    }

    public static DocumentPreviewFragment newInstance(File path) {
        DocumentPreviewFragment f = new DocumentPreviewFragment();
        Bundle args = new Bundle();
        args.putSerializable(DocumentIO.EXTRA_PATH, path);
        f.setArguments(args);
        return f;
    }

    @BindView(R.id.preview__activity__webview)
    WebView _webView;

    private View _view;
    private Context _context;
    private Document _document;

    public DocumentPreviewFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.document__fragment__preview, container, false);
        ButterKnife.bind(this, view);
        _view = view;
        _context = view.getContext();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }

        _document = loadDocument();
        showDocument();
    }

    private void showDocument() {
        Activity activity = getActivity();
        if (activity != null && activity instanceof DocumentActivity) {
            DocumentActivity da = ((DocumentActivity) activity);
            da.setDocumentTitle(_document.getTitle());
            da.setDocument(_document);
        }
        MarkdownConverter.convertToHtmlRenderIntoWebview(_document, _webView);

        WebView _webview = _webView;
        _webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("file://")) {
                    File file = new File(url.replace("file://", ""));
                    String mimetype;
                    if (ContextUtils.get().isMaybeMarkdownFile(file)) {
                        //TODO: start new DocumentPreview fragment
                    } else if ((mimetype = ContextUtils.getMimeType(url)) != null) {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file), mimetype);
                        startActivity(intent);
                    } else {
                        Uri uri = Uri.parse(url);
                        startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, uri), getString(R.string.open_with)));
                    }
                } else {
                    ContextUtils.get().openWebpageInExternalBrowser(url);
                }
                return true;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.document__preview__menu, menu);
        ContextUtils cu = ContextUtils.get();

        cu.tintMenuItems(menu, true, Color.WHITE);
        cu.setSubMenuIconsVisiblity(menu, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit: {
                // Handled by parent
                return false;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private Document loadDocument() {
        return DocumentIO.loadDocument(getActivity(), getArguments(), _document);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showEditOnBack = false;
    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }

    @Override
    public boolean onBackPressed() {
        if (showEditOnBack) {
            showEditOnBack = false;
            Activity activity = getActivity();
            if (activity != null && activity instanceof DocumentActivity) {
                DocumentActivity da = ((DocumentActivity) activity);
                da.showEditor(_document, null, false);
            }
            return true;
        }
        return false;
    }

    public WebView getWebview() {
        return _webView;
    }
}
