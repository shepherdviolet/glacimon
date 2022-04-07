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

import sviolet.thistle.util.file.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ImageCaptchaTest {

    public static void main(String[] args) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageCaptchaUtils.drawImage("Za1s", outputStream, ImageCaptchaUtils.OPTIONS_LARGE_320_100_EASY_COLORFUL);
        FileUtils.writeBytes(new File("E:\\__Temp\\test.png"), outputStream.toByteArray(), false);
    }

}
