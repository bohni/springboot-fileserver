package de.codereview.springboot.fileserver.service.converter;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ConverterService
{
    private static final Logger log = LoggerFactory.getLogger(ConverterService.class);

    private Map<String, Converter> converters = new HashMap<>();

    public ConverterService() {
        registerConverter(new MarkdownHtml());
        // TODO: externalize configuration
    }

    private void registerConverter(Converter converter)
    {
        converters.put(converter.getSource() + ";" + converter.getTarget(), converter);
    }

    public boolean isConversionAvailable(String source, String target) {
        return getConverter(source, target) != null;
    }

    private Converter getConverter(String source, String target) {
        String key = source + ";" + target;
        return converters.get(key.intern());
    }

    public Result convert(byte[] data, String source, String target, String filename) {
        Converter converter = getConverter(source, target);
        if (converter==null) {
            String message = String.format("no converter from %s to %s registered.", source, target);
            log.error(message);
            throw new RuntimeException(message);
        }
        return converter.convert(data, filename);
    }

    /**
     * give file extension for given mimetype
     * @param target mimetype
     * @return file extension according to Apache Tika
     */
    public String getTargetExtension(String target) {
        String extension;
        try
        {
            TikaConfig config = TikaConfig.getDefaultConfig();
            MimeType mimeType = config.getMimeRepository().forName(MediaType.parse(target).toString());
            extension = mimeType.getExtension();
        } catch (MimeTypeException e)
        {
            log.error("Error finding extension for mimetype from tika");
            extension = "";
        }
        return extension;
    }
}
