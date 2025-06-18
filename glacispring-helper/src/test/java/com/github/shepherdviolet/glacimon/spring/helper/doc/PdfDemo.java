/*
 * Copyright (C) 2022-2023 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/glacimon
 * Email: shepherdviolet@163.com
 */

package com.github.shepherdviolet.glacimon.spring.helper.doc;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import com.github.shepherdviolet.glacimon.java.io.FileUtils;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.styledxmlparser.jsoup.Jsoup;
import com.itextpdf.styledxmlparser.jsoup.nodes.Document;
import com.itextpdf.styledxmlparser.jsoup.nodes.Element;
import com.itextpdf.styledxmlparser.jsoup.select.Elements;
import fr.opensagres.poi.xwpf.converter.xhtml.Base64EmbedImgManager;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLConverter;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * PDF转换/编辑示例
 *
 * 依赖:
 *             "org.apache.poi:poi-ooxml:5.4.1",
 *             "org.apache.poi:poi-ooxml-full:5.4.1",
 *             "fr.opensagres.xdocreport:xdocreport:2.0.4",
 *             "com.itextpdf:html2pdf:5.0.1", //not for commercial use
 */
public class PdfDemo {

    /**
     * 测试
     */
    public static void main(String[] args) throws Exception {

        String path = "D:/__Temp/pdf-test";

        try (InputStream inputStream = new FileInputStream(path + "/template.docx");
             OutputStream outputStream = new FileOutputStream(path + "/result.pdf")) {
            String html = docxToHtml(inputStream, new HashMap<>());
            FileUtils.writeString(new File(path + "/template.html"), html, false);
            html2Pdf(html, outputStream);
        }

    }

    /**
     * docx转html, word2007+
     * @param docxInputStream docx输入流
     * @param replaceMap 字符串替换表
     * @return html
     */
    public static String docxToHtml(InputStream docxInputStream, Map<String, String> replaceMap) throws IOException {

        // 加下docx文件 (只支持docx格式, doc不支持)
        XWPFDocument xwpfDocument = new XWPFDocument(docxInputStream);

        // 配置
        XHTMLOptions xhtmlOptions = XHTMLOptions.create()
                .setImageManager(new Base64EmbedImgManager())// 带图片的word，则将图片转为base64编码，保存在一个页面中
                .setFragment(true)// 若设置true,表示外层没有html\head\body标签，设为false，保留html等标签。
                .indent(3);// 缩进3

        // 转换为html
        StringWriter stringWriter = new StringWriter();
        ((XHTMLConverter) XHTMLConverter.getInstance()).convert(xwpfDocument, stringWriter, xhtmlOptions);
        String html = stringWriter.toString();

        // 解析html, 优化布局
        Document doc = Jsoup.parseBodyFragment(html);

        // 去掉页边距和固定宽度
        Elements elements = doc.getElementsByTag("div");
        for (Element element : elements) {
            element.attr("style", "line-height:2em;margin-left: 3em; margin-right: 3em;");
        }

        //设置所有table的样式
        Elements tables = doc.getElementsByTag("table");
        for (Element table : tables) {
            table.attr("style", "border-collapse: collapse;");
        }

        // 文本替换
        html = doc.outerHtml();
        for (Map.Entry<String, String> item : replaceMap.entrySet()) {
            html = html.replace(item.getKey(), item.getValue());
        }

        return html;
    }

    /**
     *  html转pdf
     * @param html html
     * @param output pdf输出流
     */
    public static void html2Pdf(String html, OutputStream output) throws IOException {
        //转换时参数配置，例如字体,依赖图片等文件路径
        ConverterProperties properties = new ConverterProperties();

        // 使用系统字体
//        FontProvider fp = new DefaultFontProvider(true, true, true);

        // 使用系统字体和指定字体
        FontProvider fp = new FontProvider();
//        fp.addStandardPdfFonts(); //PDF标准字体
//        fp.addSystemFonts(); //系统字体
        fp.addFont(FontProgramFactory.createFont("C:\\Windows\\Fonts\\simfang.ttf")); //Windows自带字体(仿宋)
//        fp.addFont(FontProgramFactory.createFont("fonts/simfang.ttf")); //Classpath下的字体

        // 使用asia包的字体, 需要依赖: com.itextpdf:font-asian:8.0.1
//        FontProvider fp = new FontProvider();
//        fp.addFont(FontProgramFactory.createFont("STSong-Light"), "UniGB-UCS2-H");

        properties.setFontProvider(fp);

        //转换
        try (PdfDocument pdfDocument = new PdfDocument(new PdfWriter(output))) {
            //设置pdf页面大小为A4
            pdfDocument.setDefaultPageSize(PageSize.A4);
            HtmlConverter.convertToPdf(html, pdfDocument, properties);
        }
    }

}
