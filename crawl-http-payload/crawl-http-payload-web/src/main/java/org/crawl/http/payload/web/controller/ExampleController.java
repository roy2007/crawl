package org.crawl.http.payload.web.controller;

import static org.crawl.http.payload.web.base.Result.SUCCESS_CODE;
import static org.crawl.http.payload.web.base.Result.SUCCESS_MSG;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.crawl.http.payload.configure.ConfigureInfo;
import org.crawl.http.payload.service.ExampleService;
import org.crawl.http.payload.vo.ReferenceCountedWithInputStreamVO;
import org.crawl.http.payload.web.base.Result;
import org.crawl.http.payload.web.utils.LocalCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Roy
 * @date 2020/11/02
 */

@RestController
@RequestMapping(value = "/example")
public class ExampleController {

    public ExampleController(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    /**
     * 获取配置的IP 和ID
     *
     * @return {@link Result}
     */
    @GetMapping(value = "/config")
    public Result configInfo() {
        String currentThreadName = Thread.currentThread ().getName ();
        String mymakes = LocalCache.getData ("roy", new LocalCache.Load<String> (){
            @Override
            public String load () {
                String mymakes = "roy.rui20071229";// XXXXService.queryCountUsers(parameterMap);
                return mymakes;
            }
        }, 10);
        String fileFullPath = "F:\\wkdir\\word_test\\数据处理软件使用手册(国家版本).docx";
        ReferenceCountedWithInputStreamVO fileHandlerForIn = LocalCache.getData (fileFullPath,
                        new LocalCache.Load<ReferenceCountedWithInputStreamVO> (){
            @Override
                            public ReferenceCountedWithInputStreamVO load () {
                try {
                    InputStream in = new FileInputStream (fileFullPath);
                                    ReferenceCountedWithInputStreamVO referenceCountedWithInputStreamVO = new ReferenceCountedWithInputStreamVO (
                                                    in);
                                    return referenceCountedWithInputStreamVO;
                } catch (FileNotFoundException e) {
                    e.printStackTrace ();
                }
                return null;
            }
        }, 10);
        try {
            // System.out.println (
            // "------文 件大小--------" + currentThreadName + "|" + fileHandlerForIn.retain ().available ());
            System.out.println ("======文件引用次数==" + currentThreadName + "|" + fileHandlerForIn.getRefrenceCount ());
            System.out.println (
                            "-------文 件对象内存标识--" + currentThreadName + "|" + fileHandlerForIn.retain ().toString ());
            // fileHandlerForIn.close ();
        } catch (Exception e) {
            e.printStackTrace ();
        } finally {
            // try {
            // fileHandlerForIn.close ();
            // } catch (IOException e) {
            // e.printStackTrace ();
            // }
        }
        // 获取配置信息
        ConfigureInfo configureInfo = exampleService.configInfo();
        configureInfo.setOthers (mymakes);
        // 封装返回
        return Result.builder().code(SUCCESS_CODE).msg(SUCCESS_MSG).data(configureInfo).build();
    }

    @GetMapping("/input")
    public String input(String word){
        return exampleService.configInfo().toString();
    }

    @GetMapping("/wrap")
    public String word(String word){
        return exampleService.wrap(word);
    }

    /**
     * 注入 ExampleService
     */
    @Autowired
    private ExampleService exampleService;

    @PostMapping("/wrap2")
    public String word2(String word){
        return exampleService.wrap(word);
    }
}
