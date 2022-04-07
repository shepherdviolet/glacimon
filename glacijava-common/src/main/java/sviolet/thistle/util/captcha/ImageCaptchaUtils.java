/*
 * Copyright (C) 2015-2021 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.captcha;

import sviolet.thistle.util.common.CloseableUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ImageCaptchaUtils
 *
 * @author shepherdviolet
 */
public class ImageCaptchaUtils {

    // statics ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final Font[] FONTS_DEFAULT = {
            new Font("Dialog", Font.PLAIN, 10),
            new Font("DialogInput", Font.PLAIN, 10),
            new Font("Monospaced", Font.PLAIN, 10),
            new Font("Serif", Font.PLAIN, 10),
            new Font("SansSerif", Font.PLAIN, 10)};

    private static final Font[] FONTS_EASY = {
            new Font("Dialog", Font.PLAIN, 10),
            new Font("SansSerif", Font.PLAIN, 10)};

    private static final Font[] FONTS_CN = {
            new Font("宋体", Font.PLAIN, 10),
            new Font("华文楷体", Font.PLAIN, 10),
            new Font("黑体", Font.PLAIN, 10),
            new Font("微软雅黑", Font.PLAIN, 10),
            new Font("楷体_GB2312", Font.PLAIN, 10)};

    // options ////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 320*100 easy colourful
     */
    public static final Options OPTIONS_LARGE_320_100_EASY_COLORFUL = new OptionsBuilder()
            .imageFormat("png")
            .backgroundSize(320, 100)
            .backgroundColor(250, 250, 250, 255)
            .randomFontColor(0, 180, 0, 180, 0, 180)
            .randomFonts(FONTS_DEFAULT)
            .randomFontAffineTransform(0.7f, 0.9f, -0.3f, 0.3f, -0.1f, 0.1f)
            .interferenceLine(6, 8, 1, 2)
            .interferenceGranule(80, 100, 1, 3, 6)
            .build();

    /**
     * 320*100 easy black
     */
    public static final Options OPTIONS_LARGE_320_100_EASY_BLACK = new OptionsBuilder()
            .imageFormat("png")
            .backgroundSize(320, 100)
            .backgroundColor(250, 250, 250, 255)
            .randomFontColor(0, 40, 0, 40, 0, 40)
            .randomFonts(FONTS_DEFAULT)
            .randomFontAffineTransform(0.7f, 0.9f, -0.3f, 0.3f, -0.1f, 0.1f)
            .interferenceLine(6, 8, 1, 2)
            .interferenceGranule(80, 100, 1, 3, 6)
            .build();

    /**
     * 320*100 easy CN fonts
     */
    public static final Options OPTIONS_LARGE_320_100_EASY_CN = new OptionsBuilder()
            .imageFormat("png")
            .backgroundSize(320, 100)
            .backgroundColor(250, 250, 250, 255)
            .randomFontColor(0, 180, 0, 180, 0, 180)
            .randomFonts(FONTS_CN)
            .randomFontAffineTransform(0.7f, 0.9f, -0.3f, 0.3f, -0.1f, 0.1f)
            .interferenceLine(6, 8, 1, 2)
            .interferenceGranule(80, 100, 1, 3, 6)
            .build();

    /**
     * 360*100 hard colourful
     */
    public static final Options OPTIONS_LARGE_360_100_HARD_COLORFUL = new OptionsBuilder()
            .imageFormat("png")
            .backgroundSize(360, 100)
            .backgroundColor(250, 250, 250, 255)
            .randomFontColor(0, 180, 0, 180, 0, 180)
            .randomFonts(FONTS_DEFAULT)
            .randomFontAffineTransform(0.6f, 0.8f, -0.4f, 0.4f, -0.1f, 0.1f)
            .interferenceLine(7, 9, 1, 2)
            .interferenceGranule(120, 140, 1, 3, 6)
            .build();

    /**
     * 360*100 hard black
     */
    public static final Options OPTIONS_LARGE_360_100_HARD_BLACK = new OptionsBuilder()
            .imageFormat("png")
            .backgroundSize(360, 100)
            .backgroundColor(250, 250, 250, 255)
            .randomFontColor(0, 40, 0, 40, 0, 40)
            .randomFonts(FONTS_DEFAULT)
            .randomFontAffineTransform(0.6f, 0.8f, -0.4f, 0.4f, -0.1f, 0.1f)
            .interferenceLine(7, 9, 1, 2)
            .interferenceGranule(120, 140, 1, 3, 6)
            .build();

    /**
     * 360*100 hard CN fonts
     */
    public static final Options OPTIONS_LARGE_360_100_HARD_CN = new OptionsBuilder()
            .imageFormat("png")
            .backgroundSize(360, 100)
            .backgroundColor(250, 250, 250, 255)
            .randomFontColor(0, 180, 0, 180, 0, 180)
            .randomFonts(FONTS_CN)
            .randomFontAffineTransform(0.6f, 0.8f, -0.4f, 0.4f, -0.1f, 0.1f)
            .interferenceLine(7, 9, 1, 2)
            .interferenceGranule(120, 140, 1, 3, 6)
            .build();

    /**
     * 160*50 easy colorful
     */
    public static final Options OPTIONS_MEDIUM_160_50_EASY_COLORFUL = new OptionsBuilder()
            .imageFormat("png")
            .backgroundSize(160, 50)
            .backgroundColor(250, 250, 250, 255)
            .randomFontColor(0, 150, 0, 150, 0, 150)
            .randomFonts(FONTS_DEFAULT)
            .randomFontAffineTransform(0.9f, 1.0f, -0.25f, 0.25f, -0.05f, 0.05f)
            .interferenceLine(6, 8, 1, 1)
            .interferenceGranule(40, 60, 1, 1, 4)
            .build();

    /**
     * 160*50 easy black
     */
    public static final Options OPTIONS_MEDIUM_160_50_EASY_BLACK = new OptionsBuilder()
            .imageFormat("png")
            .backgroundSize(160, 50)
            .backgroundColor(250, 250, 250, 255)
            .randomFontColor(0, 40, 0, 40, 0, 40)
            .randomFonts(FONTS_DEFAULT)
            .randomFontAffineTransform(0.9f, 1.0f, -0.25f, 0.25f, -0.05f, 0.05f)
            .interferenceLine(6, 8, 1, 1)
            .interferenceGranule(40, 60, 1, 1, 4)
            .build();

    /**
     * 160*50 easy cn
     */
    public static final Options OPTIONS_MEDIUM_160_50_EASY_CN = new OptionsBuilder()
            .imageFormat("png")
            .backgroundSize(160, 50)
            .backgroundColor(250, 250, 250, 255)
            .randomFontColor(0, 150, 0, 150, 0, 150)
            .randomFonts(FONTS_CN)
            .randomFontAffineTransform(0.9f, 1.0f, -0.25f, 0.25f, -0.05f, 0.05f)
            .interferenceLine(6, 8, 1, 1)
            .interferenceGranule(40, 60, 1, 1, 4)
            .build();

    /**
     * 180*50 hard colorful
     */
    public static final Options OPTIONS_MEDIUM_180_50_HARD_COLORFUL = new OptionsBuilder()
            .imageFormat("png")
            .backgroundSize(180, 50)
            .backgroundColor(250, 250, 250, 255)
            .randomFontColor(0, 150, 0, 150, 0, 150)
            .randomFonts(FONTS_DEFAULT)
            .randomFontAffineTransform(0.9f, 1.0f, -0.35f, 0.35f, -0.05f, 0.05f)
            .interferenceLine(6, 8, 1, 1)
            .interferenceGranule(50, 70, 1, 2, 4)
            .build();

    /**
     * 180*50 hard black
     */
    public static final Options OPTIONS_MEDIUM_180_50_HARD_BLACK = new OptionsBuilder()
            .imageFormat("png")
            .backgroundSize(180, 50)
            .backgroundColor(250, 250, 250, 255)
            .randomFontColor(0, 40, 0, 40, 0, 40)
            .randomFonts(FONTS_DEFAULT)
            .randomFontAffineTransform(0.9f, 1.0f, -0.35f, 0.35f, -0.05f, 0.05f)
            .interferenceLine(6, 8, 1, 1)
            .interferenceGranule(50, 70, 1, 2, 4)
            .build();

    /**
     * 180*50 hard CN fonts
     */
    public static final Options OPTIONS_MEDIUM_180_50_HARD_CN = new OptionsBuilder()
            .imageFormat("png")
            .backgroundSize(180, 50)
            .backgroundColor(250, 250, 250, 255)
            .randomFontColor(0, 150, 0, 150, 0, 150)
            .randomFonts(FONTS_CN)
            .randomFontAffineTransform(0.9f, 1.0f, -0.35f, 0.35f, -0.05f, 0.05f)
            .interferenceLine(6, 8, 1, 1)
            .interferenceGranule(50, 70, 1, 2, 4)
            .build();

    /**
     * 90*25 easy colorful
     */
    public static final Options OPTIONS_SMALL_90_25_EASY_COLORFUL = new OptionsBuilder()
            .imageFormat("png")
            .backgroundSize(90, 25)
            .backgroundColor(250, 250, 250, 255)
            .randomFontColor(0, 150, 0, 150, 0, 150)
            .randomFonts(FONTS_EASY)
            .randomFontAffineTransform(0.9f, 1.0f, 0f, 0f, -0.15f, 0.15f)
            .interferenceLine(3, 4, 1, 1)
            .interferenceGranule(20, 30, 1, 1, 3)
            .build();

    /**
     * 90*25 easy black
     */
    public static final Options OPTIONS_SMALL_90_25_EASY_BLACK = new OptionsBuilder()
            .imageFormat("png")
            .backgroundSize(90, 25)
            .backgroundColor(250, 250, 250, 255)
            .randomFontColor(0, 80, 0, 80, 0, 80)
            .randomFonts(FONTS_EASY)
            .randomFontAffineTransform(0.9f, 1.0f, 0f, 0f, -0.15f, 0.15f)
            .interferenceLine(3, 4, 1, 1)
            .interferenceGranule(20, 30, 1, 1, 3)
            .build();

    /**
     * draw captcha image
     * @param text captcha text
     * @param outputStream output stream
     * @param options options
     */
    public static void drawImage(String text, OutputStream outputStream, Options options) throws IOException {
        try {
            // check
            text = checkInput(text, outputStream, options);

            // basic properties
            int backgroundWidth = options.backgroundWidth;
            int backgroundHeight = options.backgroundHeight;

            // create image instance
            BufferedImage image = new BufferedImage(backgroundWidth, backgroundHeight, BufferedImage.TYPE_INT_BGR);

            // Draw background //////////////////////////////////////////////////////////////////////

            // draw background
            Graphics2D graphics = image.createGraphics();
            graphics.setBackground(options.backgroundColor);
            graphics.clearRect(0, 0, backgroundWidth, backgroundHeight);
            graphics.dispose();

            // Draw text  ///////////////////////////////////////////////////////////////////////////

            int textLength = text.length();
            if (textLength > 0) {

                // text container image
                BufferedImage textImage = new BufferedImage(backgroundWidth, backgroundHeight, BufferedImage.TYPE_4BYTE_ABGR);

                int wordMaxWidth = backgroundWidth / textLength;
                //noinspection UnnecessaryLocalVariable
                int wordMaxHeight = backgroundHeight;
                int wordSize = Math.min(wordMaxWidth, wordMaxHeight);

                // draw each words
                for (int i = 0 ; i < text.length() ; i++) {
                    String word = String.valueOf(text.charAt(i));
                    drawWord(options, textImage, wordMaxWidth, wordMaxHeight, wordSize, i, word);
                }

                // draw text image on image with global affine transform
                graphics = image.createGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.drawImage(textImage,
                        options.randomGlobalAffineTransform(backgroundWidth, backgroundHeight),
                        null);
                graphics.dispose();

            }

            // Draw interference ///////////////////////////////////////////////////////////////////////////

            // interference line
            if (options.interferenceLineNumMax > 0) {
                graphics = image.createGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Random random = ThreadLocalRandom.current();
                int interferenceLineNum = options.randomInterferenceLineNum();

                // draw each line
                for (int i = 0 ; i < interferenceLineNum ; i++) {
                    int x1 = random.nextInt(backgroundWidth / 2);
                    int y1 = random.nextInt(backgroundHeight);
                    int x2 = random.nextInt(backgroundWidth / 2) + backgroundWidth / 2;
                    int y2 = random.nextInt(backgroundHeight);
                    graphics.setColor(options.randomFontColor(random.nextInt(100) + 155));
                    graphics.setStroke(new BasicStroke(options.randomInterferenceLineStroke()));
                    graphics.drawLine(x1, y1, x2, y2);
                }

                graphics.dispose();
            }

            // interference granule
            if (options.interferenceGranuleNumMax > 0) {
                graphics = image.createGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Random random = ThreadLocalRandom.current();
                int interferenceGranuleNum = options.randomInterferenceGranuleNum();

                // draw each granule
                for (int i = 0 ; i < interferenceGranuleNum ; i++) {
                    int x1 = random.nextInt(backgroundWidth);
                    int y1 = random.nextInt(backgroundHeight);
                    int x2 = x1 + options.randomInterferenceGranuleLength();
                    int y2 = y1 + options.randomInterferenceGranuleLength();
                    graphics.setColor(options.randomFontColor(random.nextInt(100) + 155));
                    graphics.setStroke(new BasicStroke(options.randomInterferenceGranuleStroke()));
                    graphics.drawLine(x1, y1, x2, y2);
                }

                graphics.dispose();
            }

            // Output     ///////////////////////////////////////////////////////////////////////////

            // write image to output stream
            ImageIO.write(image, options.imageFormat, outputStream);

        } finally {
            CloseableUtils.closeQuiet(outputStream);
        }
    }

    /**
     * draw a word
     */
    private static void drawWord(Options options, BufferedImage textImage, int wordMaxWidth, int wordMaxHeight, int wordSize, int index, String word) {
        BufferedImage wordImage = new BufferedImage(wordMaxWidth, wordMaxHeight, BufferedImage.TYPE_4BYTE_ABGR);

        Graphics2D wordGraphics = wordImage.createGraphics();
        Font randomFont = options.randomFont(wordSize);
        int wordRealWidth = wordGraphics.getFontMetrics(randomFont).stringWidth(word);
        wordGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        wordGraphics.setFont(randomFont);
        wordGraphics.setColor(options.randomFontColor(255));
        wordGraphics.drawString(word, (wordMaxWidth - wordRealWidth) / 2, (int)((float) wordMaxHeight * 0.85f));
        wordGraphics.dispose();

        // draw text image on image with global affine transform
        Graphics2D textGraphics = textImage.createGraphics();
        textGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        textGraphics.drawImage(wordImage,
                options.randomFontAffineTransform(wordMaxWidth, wordMaxHeight, index * wordMaxWidth),
                null);
        textGraphics.dispose();
    }

    private static String checkInput(String text, OutputStream outputStream, Options options) {
        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream is null");
        }
        if (options == null) {
            throw new IllegalArgumentException("options is null");
        }
        if (text == null) {
            text = "";
        }
        return text;
    }

    /**
     * Options, created by "new OptionsBuilder().build()"
     */
    private static class Options {

        private String imageFormat;
        private int backgroundWidth;
        private int backgroundHeight;
        private Color backgroundColor;

        private int randomFontColorRedMin;
        private int randomFontColorRedMax;
        private int randomFontColorGreenMin;
        private int randomFontColorGreenMax;
        private int randomFontColorBlueMin;
        private int randomFontColorBlueMax;

        private Font[] randomFonts;

        private float randomFontAffineTransformScaleMin;
        private float randomFontAffineTransformScaleMax;
        private float randomFontAffineTransformShearMin;
        private float randomFontAffineTransformShearMax;
        private float randomFontAffineTransformTranslateRatioMin;
        private float randomFontAffineTransformTranslateRatioMax;

        private float randomGlobalAffineTransformScaleMin;
        private float randomGlobalAffineTransformScaleMax;
        private float randomGlobalAffineTransformShearMin;
        private float randomGlobalAffineTransformShearMax;
        private float randomGlobalAffineTransformTranslateRatioMin;
        private float randomGlobalAffineTransformTranslateRatioMax;

        private int interferenceLineNumMin;
        private int interferenceLineNumMax;
        private int interferenceLineStrokeMin;
        private int interferenceLineStrokeMax;

        private int interferenceGranuleNumMin;
        private int interferenceGranuleNumMax;
        private int interferenceGranuleStrokeMin;
        private int interferenceGranuleStrokeMax;
        private int interferenceGranuleLengthMax;

        private Font randomFont(int size){
            Random random = ThreadLocalRandom.current();
            // random font type
            return randomFonts[random.nextInt(randomFonts.length)].deriveFont(Font.PLAIN, size);
        }

        private Color randomFontColor(int alpha){
            Random random = ThreadLocalRandom.current();
            // random color
            return new Color(randomFontColorRedMin + random.nextInt(randomFontColorRedMax - randomFontColorRedMin + 1),
                    randomFontColorGreenMin + random.nextInt(randomFontColorGreenMax - randomFontColorGreenMin + 1),
                    randomFontColorBlueMin + random.nextInt(randomFontColorBlueMax - randomFontColorBlueMin + 1),
                    alpha);
        }

        private AffineTransform randomGlobalAffineTransform(int maxWidth, int maxHeight) {
            Random random = ThreadLocalRandom.current();
            float scaleMin = randomGlobalAffineTransformScaleMin;
            float scaleRange = randomGlobalAffineTransformScaleMax - randomGlobalAffineTransformScaleMin;
            float shearMin = randomGlobalAffineTransformShearMin;
            float shearRange = randomGlobalAffineTransformShearMax - randomGlobalAffineTransformShearMin;
            float translateRatioMin = randomGlobalAffineTransformTranslateRatioMin;
            float translateRatioRange = randomGlobalAffineTransformTranslateRatioMax - randomGlobalAffineTransformTranslateRatioMin;

            float scaleX = scaleMin + scaleRange * polarize(random.nextFloat());
            float scaleY = scaleMin + scaleRange * polarize(random.nextFloat());
            float shearX = shearMin + shearRange * polarize(random.nextFloat());
            float shearY = shearMin + shearRange * polarize(random.nextFloat());
            float translateX = maxWidth * (translateRatioMin + translateRatioRange * polarize(random.nextFloat()))
                    + maxWidth * (1f - scaleX) / 2f; // scaling offset correction
            float translateY = maxHeight * (translateRatioMin + translateRatioRange * polarize(random.nextFloat()))
                    + maxHeight * (1f - scaleY) / 2f; // scaling offset correction

            // random AffineTransform
            return new AffineTransform(scaleX, shearY, shearX, scaleY, translateX, translateY);
        }

        private AffineTransform randomFontAffineTransform(int wordMaxWidth, int wordMaxHeight, int offsetX) {
            Random random = ThreadLocalRandom.current();
            float scaleMin = randomFontAffineTransformScaleMin;
            float scaleRange = randomFontAffineTransformScaleMax - randomFontAffineTransformScaleMin;
            float shearMin = randomFontAffineTransformShearMin;
            float shearRange = randomFontAffineTransformShearMax - randomFontAffineTransformShearMin;
            float translateRatioMin = randomFontAffineTransformTranslateRatioMin;
            float translateRatioRange = randomFontAffineTransformTranslateRatioMax - randomFontAffineTransformTranslateRatioMin;

            float scaleX = scaleMin + scaleRange * polarize(random.nextFloat());
            float scaleY = scaleMin + scaleRange * polarize(random.nextFloat());
            float shearX = shearMin + shearRange * polarize(random.nextFloat());
            float shearY = shearMin + shearRange * polarize(random.nextFloat());
            float translateX = wordMaxWidth * (translateRatioMin + translateRatioRange * polarize(random.nextFloat()))
                    + wordMaxWidth * (1f - scaleX) / 2f // scaling offset correction
                    + offsetX; // offset of word
            float translateY = wordMaxHeight * (translateRatioMin + translateRatioRange * polarize(random.nextFloat()))
                    + wordMaxHeight * (1f - scaleY) / 2f; // scaling offset correction

            // random AffineTransform
            return new AffineTransform(scaleX, shearY, shearX, scaleY, translateX, translateY);
        }

        /**
         * Distribute random numbers to the outside
         */
        private float polarize(float f) {
            // Normally, there will be no situation greater than 1 and less than 0
            if (f < 0f || f > 1f) {
                return f;
            }
            if (f < 0.5f) {
                return f / 2f;
            } else {
                return 1.0f - (1.0f - f) / 2f;
            }
        }

        private int randomInterferenceLineNum(){
            return ThreadLocalRandom.current().nextInt(interferenceLineNumMax - interferenceLineNumMin + 1) + interferenceLineNumMin;
        }

        private int randomInterferenceLineStroke(){
            return ThreadLocalRandom.current().nextInt(interferenceLineStrokeMax - interferenceLineStrokeMin + 1) + interferenceLineStrokeMin;
        }

        private int randomInterferenceGranuleNum(){
            return ThreadLocalRandom.current().nextInt(interferenceGranuleNumMax - interferenceGranuleNumMin + 1) + interferenceGranuleNumMin;
        }

        private int randomInterferenceGranuleStroke(){
            return ThreadLocalRandom.current().nextInt(interferenceGranuleStrokeMax - interferenceGranuleStrokeMin + 1) + interferenceGranuleStrokeMin;
        }

        private int randomInterferenceGranuleLength(){
            return ThreadLocalRandom.current().nextInt(interferenceGranuleLengthMax * 2 + 1) - interferenceGranuleLengthMax;
        }

    }

    /**
     * OptionsBuilder
     */
    public static class OptionsBuilder {

        private String imageFormat = "png";
        private int backgroundWidth = 320;
        private int backgroundHeight = 100;
        private Color backgroundColor = new Color(250, 250, 250, 255);

        private int randomFontColorRedMin = 0;
        private int randomFontColorRedMax = 180;
        private int randomFontColorGreenMin = 0;
        private int randomFontColorGreenMax = 180;
        private int randomFontColorBlueMin = 0;
        private int randomFontColorBlueMax = 180;

        private Font[] randomFonts = FONTS_DEFAULT;

        private float randomFontAffineTransformScaleMin = 0.7f;
        private float randomFontAffineTransformScaleMax = 0.9f;
        private float randomFontAffineTransformShearMin = -0.3f;
        private float randomFontAffineTransformShearMax = 0.3f;
        private float randomFontAffineTransformTranslateRatioMin = -0.1f;
        private float randomFontAffineTransformTranslateRatioMax = 0.1f;

        private float randomGlobalAffineTransformScaleMin = 1f;
        private float randomGlobalAffineTransformScaleMax = 1f;
        private float randomGlobalAffineTransformShearMin = 0f;
        private float randomGlobalAffineTransformShearMax = 0f;
        private float randomGlobalAffineTransformTranslateRatioMin = 0f;
        private float randomGlobalAffineTransformTranslateRatioMax = 0f;

        private int interferenceLineNumMin = 6;
        private int interferenceLineNumMax = 8;
        private int interferenceLineStrokeMin = 1;
        private int interferenceLineStrokeMax = 2;

        private int interferenceGranuleNumMin = 60;
        private int interferenceGranuleNumMax = 80;
        private int interferenceGranuleStrokeMin = 1;
        private int interferenceGranuleStrokeMax = 3;
        private int interferenceGranuleLengthMax = 6;

        public OptionsBuilder() {
        }

        /**
         * @param imageFormat Image format, png, jpg ...
         */
        public OptionsBuilder imageFormat(String imageFormat) {
            this.imageFormat = imageFormat;
            return this;
        }

        /**
         * @param width captcha width
         * @param height captcha height
         */
        public OptionsBuilder backgroundSize(int width, int height) {
            if (width < 1) {
                throw new IllegalArgumentException("backgroundSize width cannot be less than 1");
            }
            if (height < 1) {
                throw new IllegalArgumentException("backgroundSize height cannot be less than 1");
            }
            this.backgroundWidth = width;
            this.backgroundHeight = height;
            return this;
        }

        /**
         * background color
         * @param red red, [0, 255]
         * @param green green, [0, 255]
         * @param blue blue, [0, 255]
         * @param alpha alpha, [0, 255]
         */
        public OptionsBuilder backgroundColor(int red, int green, int blue, int alpha) {
            checkColorInt(red, "backgroundColor red");
            checkColorInt(green, "backgroundColor green");
            checkColorInt(blue, "backgroundColor blue");
            checkColorInt(alpha, "backgroundColor alpha");
            this.backgroundColor = new Color(red, green, blue, alpha);
            return this;
        }

        /**
         * random font color
         * @param redMin red min value, [0, 255]
         * @param redMax red max value, [0, 255]
         * @param greenMin green min value, [0, 255]
         * @param greenMax green max value, [0, 255]
         * @param blueMin blue min value, [0, 255]
         * @param blueMax blue max value, [0, 255]
         */
        public OptionsBuilder randomFontColor(int redMin, int redMax,
                                              int greenMin, int greenMax,
                                              int blueMin, int blueMax){
            checkColorMinMaxInt(redMin, redMax, "randomFontColor red");
            checkColorMinMaxInt(greenMin, greenMax, "randomFontColor green");
            checkColorMinMaxInt(blueMin, blueMax, "randomFontColor blue");
            this.randomFontColorRedMin = redMin;
            this.randomFontColorRedMax = redMax;
            this.randomFontColorGreenMin = greenMin;
            this.randomFontColorGreenMax = greenMax;
            this.randomFontColorBlueMin = blueMin;
            this.randomFontColorBlueMax = blueMax;
            return this;
        }

        /**
         * <p>Set text fonts, the style and size will change randomly, so the setting (style and size) here is invalid.</p>
         *
         * <p>How to load custom font (TTF):</p>
         * <p>Font font = Font.createFont(Font.TRUETYPE_FONT, inputStream);</p>
         *
         * @param fonts Example: new Font[]{
         *                 new Font("Dialog", Font.PLAIN, 10),
         *                 new Font("DialogInput", Font.PLAIN, 10),
         *                 new Font("Monospaced", Font.PLAIN, 10),
         *                 new Font("Serif", Font.PLAIN, 10),
         *                 new Font("SansSerif", Font.PLAIN, 10)}
         */
        public OptionsBuilder randomFonts(Font[] fonts){
            if (fonts == null || fonts.length <= 0) {
                throw new IllegalArgumentException("null or empty fonts");
            }
            this.randomFonts = fonts;
            return this;
        }

        /**
         * random affine transform for each word, 每个字随机缩放/扭曲/平移
         * @param scaleMin scale min value, (0, 1], 缩放最小值
         * @param scaleMax scale max value, (0, 1], 缩放最大值
         * @param shearMin shear min value, [-1, 1], 扭曲最小值
         * @param shearMax shear max value, [-1, 1], 扭曲最大值
         * @param translateRatioMin translate ratio min value, [-1, 1], 平移最小值(相对于背景宽高的比值)
         * @param translateRatioMax translate ratio max value, [-1, 1], 平移最大值(相对于背景宽高的比值)
         */
        public OptionsBuilder randomFontAffineTransform(float scaleMin, float scaleMax,
                                                        float shearMin, float shearMax,
                                                        float translateRatioMin, float translateRatioMax){
            checkAffineTransformMinMaxPositiveFloat(scaleMin, scaleMax, "randomFontAffineTransform scale");
            checkAffineTransformMinMaxFloat(shearMin, shearMax, "randomFontAffineTransform shear");
            checkAffineTransformMinMaxFloat(translateRatioMin, translateRatioMax, "randomFontAffineTransform translateRatio");
            this.randomFontAffineTransformScaleMin = scaleMin;
            this.randomFontAffineTransformScaleMax = scaleMax;
            this.randomFontAffineTransformShearMin = shearMin;
            this.randomFontAffineTransformShearMax = shearMax;
            this.randomFontAffineTransformTranslateRatioMin = translateRatioMin;
            this.randomFontAffineTransformTranslateRatioMax = translateRatioMax;
            return this;
        }

        /**
         * random affine transform for global, 整体随机缩放/扭曲/平移
         * @param scaleMin scale min value, (0, 1], 缩放最小值
         * @param scaleMax scale max value, (0, 1], 缩放最大值
         * @param shearMin shear min value, [-1, 1], 扭曲最小值
         * @param shearMax shear max value, [-1, 1], 扭曲最大值
         * @param translateRatioMin translate ratio min value, [-1, 1], 平移最小值(相对于背景宽高的比值)
         * @param translateRatioMax translate ratio max value, [-1, 1], 平移最大值(相对于背景宽高的比值)
         */
        public OptionsBuilder randomGlobalAffineTransform(float scaleMin, float scaleMax,
                                                          float shearMin, float shearMax,
                                                          float translateRatioMin, float translateRatioMax){
            checkAffineTransformMinMaxPositiveFloat(scaleMin, scaleMax, "randomGlobalAffineTransform scale");
            checkAffineTransformMinMaxFloat(shearMin, shearMax, "randomGlobalAffineTransform shear");
            checkAffineTransformMinMaxFloat(translateRatioMin, translateRatioMax, "randomGlobalAffineTransform translateRatio");
            this.randomGlobalAffineTransformScaleMin = scaleMin;
            this.randomGlobalAffineTransformScaleMax = scaleMax;
            this.randomGlobalAffineTransformShearMin = shearMin;
            this.randomGlobalAffineTransformShearMax = shearMax;
            this.randomGlobalAffineTransformTranslateRatioMin = translateRatioMin;
            this.randomGlobalAffineTransformTranslateRatioMax = translateRatioMax;
            return this;
        }

        /**
         * interference line | 干扰线
         * @param lineNumMin min num
         * @param lineNumMax max num
         * @param lineStrokeMin min stroke
         * @param lineStrokeMax max stroke
         */
        public OptionsBuilder interferenceLine(int lineNumMin, int lineNumMax, int lineStrokeMin, int lineStrokeMax) {
            checkMinMaxInt(lineNumMin, lineNumMax, "interferenceLine lineNum");
            checkMinMaxInt(lineStrokeMin, lineStrokeMax, "interferenceLine lineStroke");
            this.interferenceLineNumMin = lineNumMin;
            this.interferenceLineNumMax = lineNumMax;
            this.interferenceLineStrokeMin = lineStrokeMin;
            this.interferenceLineStrokeMax = lineStrokeMax;
            return this;
        }

        /**
         * interference granule | 干扰点
         * @param granuleNumMin min num
         * @param granuleNumMax max num
         * @param granuleStrokeMin min stroke
         * @param granuleStrokeMax max stroke
         * @param granuleLengthMax max length
         */
        public OptionsBuilder interferenceGranule(int granuleNumMin, int granuleNumMax, int granuleStrokeMin, int granuleStrokeMax, int granuleLengthMax) {
            checkMinMaxInt(granuleNumMin, granuleNumMax, "interferenceGranule granuleNum");
            checkMinMaxInt(granuleStrokeMin, granuleStrokeMax, "interferenceGranule granuleStroke");
            if (granuleLengthMax < 0) {
                throw new IllegalArgumentException("interferenceGranule granuleLengthMax cannot less than 0");
            }
            this.interferenceGranuleNumMin = granuleNumMin;
            this.interferenceGranuleNumMax = granuleNumMax;
            this.interferenceGranuleStrokeMin = granuleStrokeMin;
            this.interferenceGranuleStrokeMax = granuleStrokeMax;
            this.interferenceGranuleLengthMax = granuleLengthMax;
            return this;
        }

        public Options build(){
            Options options = new Options();
            options.imageFormat = imageFormat;
            options.backgroundWidth = backgroundWidth;
            options.backgroundHeight = backgroundHeight;
            options.backgroundColor = backgroundColor;

            options.randomFontColorRedMin = randomFontColorRedMin;
            options.randomFontColorRedMax = randomFontColorRedMax;
            options.randomFontColorGreenMin = randomFontColorGreenMin;
            options.randomFontColorGreenMax = randomFontColorGreenMax;
            options.randomFontColorBlueMin = randomFontColorBlueMin;
            options.randomFontColorBlueMax = randomFontColorBlueMax;

            options.randomFonts = randomFonts;

            options.randomFontAffineTransformScaleMin = randomFontAffineTransformScaleMin;
            options.randomFontAffineTransformScaleMax = randomFontAffineTransformScaleMax;
            options.randomFontAffineTransformShearMin = randomFontAffineTransformShearMin;
            options.randomFontAffineTransformShearMax = randomFontAffineTransformShearMax;
            options.randomFontAffineTransformTranslateRatioMin = randomFontAffineTransformTranslateRatioMin;
            options.randomFontAffineTransformTranslateRatioMax = randomFontAffineTransformTranslateRatioMax;

            options.randomGlobalAffineTransformScaleMin = randomGlobalAffineTransformScaleMin;
            options.randomGlobalAffineTransformScaleMax = randomGlobalAffineTransformScaleMax;
            options.randomGlobalAffineTransformShearMin = randomGlobalAffineTransformShearMin;
            options.randomGlobalAffineTransformShearMax = randomGlobalAffineTransformShearMax;
            options.randomGlobalAffineTransformTranslateRatioMin = randomGlobalAffineTransformTranslateRatioMin;
            options.randomGlobalAffineTransformTranslateRatioMax = randomGlobalAffineTransformTranslateRatioMax;

            options.interferenceLineNumMin = interferenceLineNumMin;
            options.interferenceLineNumMax = interferenceLineNumMax;
            options.interferenceLineStrokeMin = interferenceLineStrokeMin;
            options.interferenceLineStrokeMax = interferenceLineStrokeMax;

            options.interferenceGranuleNumMin = interferenceGranuleNumMin;
            options.interferenceGranuleNumMax = interferenceGranuleNumMax;
            options.interferenceGranuleStrokeMin = interferenceGranuleStrokeMin;
            options.interferenceGranuleStrokeMax = interferenceGranuleStrokeMax;
            options.interferenceGranuleLengthMax = interferenceGranuleLengthMax;

            return options;
        }

        private void checkColorInt(int colorValue, String colorName){
            if (colorValue < 0 || colorValue > 255) {
                throw new IllegalArgumentException(colorName + " out of range, range: [0, 255]");
            }
        }

        private void checkColorMinMaxInt(int colorMinValue, int colorMaxValue, String colorName){
            if (colorMinValue < 0 || colorMinValue > 255) {
                throw new IllegalArgumentException(colorName + "Min out of range, range: [0, 255]");
            }
            if (colorMaxValue < 0 || colorMaxValue > 255) {
                throw new IllegalArgumentException(colorName + "Max out of range, range: [0, 255]");
            }
            if (colorMinValue > colorMaxValue) {
                throw new IllegalArgumentException(colorName + " min value cannot be greater than max value");
            }
        }

        private void checkAffineTransformMinMaxPositiveFloat(float minValue, float maxValue, String name){
            if (minValue <= 0.0f || minValue > 1.0f) {
                throw new IllegalArgumentException(name + " min value out of range, range: (0.0f, 1.0f]");
            }
            if (maxValue <= 0.0f || maxValue > 1.0f) {
                throw new IllegalArgumentException(name + " max value out of range, range: (0.0f, 1.0f]");
            }
            if (minValue > maxValue) {
                throw new IllegalArgumentException(name + " min value cannot be greater than max value");
            }
        }

        private void checkAffineTransformMinMaxFloat(float minValue, float maxValue, String name){
            if (minValue < -1.0f || minValue > 1.0f) {
                throw new IllegalArgumentException(name + " min value out of range, range: [-1.0f, 1.0f]");
            }
            if (maxValue < -1.0f || maxValue > 1.0f) {
                throw new IllegalArgumentException(name + " max value out of range, range: [-1.0f, 1.0f]");
            }
            if (minValue > maxValue) {
                throw new IllegalArgumentException(name + " min value cannot be greater than max value");
            }
        }

        private void checkMinMaxInt(int minValue, int maxValue, String name){
            if (minValue < 0) {
                throw new IllegalArgumentException(name + "Min out of range, range: [0, 255]");
            }
            if (maxValue < 0) {
                throw new IllegalArgumentException(name + "Max out of range, range: [0, 255]");
            }
            if (minValue > maxValue) {
                throw new IllegalArgumentException(name + " min value cannot be greater than max value");
            }
        }

    }

}