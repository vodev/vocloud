package cz.mrq.vocloud.uwsparser;

import cz.mrq.vocloud.uwsparser.model.UWSJob;

/**
 * Singleton with UWSParser instance
 *
 * @author voadmin
 */
public class UWSParserManager {

    private static UWSParserManager instance = null;
    private UWSParser parser = null;

    /**
     * singleton
     */
    private UWSParserManager() {
        parser = new UWSParser();
    }

    public static UWSParserManager getInstance() {
        if (instance == null) {
            instance = new UWSParserManager();
        }
        return instance;
    }

    public UWSParser getParser() {
        return parser;
    }

    public UWSJob parseJob(String xml) {
        return this.getParser().parseJob(xml);
    }
}
