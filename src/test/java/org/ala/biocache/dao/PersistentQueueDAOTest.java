package org.ala.biocache.dao;

import javax.inject.Inject;

import org.ala.biocache.dto.DownloadDetailsDTO;
import org.ala.biocache.dto.DownloadDetailsDTO.DownloadType;
import org.ala.biocache.dto.DownloadRequestParams;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import junit.framework.TestCase;

public class PersistentQueueDAOTest extends TestCase{
    //protected static final DB4OPersistentQueueDAOImpl queueDAO= new DB4OPersistentQueueDAOImpl("/tmp/db4o.test");
    protected static final JsonPersistentQueueDAOImpl queueDAO = new JsonPersistentQueueDAOImpl();
    static{
        //FileUtils.deleteQuietly(new java.io.File("/tmp/db4o.test"));
        FileUtils.deleteQuietly(new java.io.File("/data/cache/downloads"));
        queueDAO.init();
        
    }

    private DownloadRequestParams getParams(String query){
        DownloadRequestParams d = new DownloadRequestParams();
        d.setQ(query);
        d.setFile("Testing");
        d.setEmail("natasha.carter@csiro.au");
        return d;
    }
    
    @Test
    public void testAdd(){
        DownloadDetailsDTO dd = new DownloadDetailsDTO(getParams("test1"), "127.0.0.1", DownloadType.FACET);
        
        queueDAO.addDownloadToQueue(dd);
        assertEquals(1,queueDAO.getTotalDownloads());
        DownloadDetailsDTO dd2 = new DownloadDetailsDTO(getParams("test2"), "127.0.0.1", DownloadType.FACET);
        
        queueDAO.addDownloadToQueue(dd2);
        assertEquals(2,queueDAO.getTotalDownloads());
        //now test that they are persisted
        queueDAO.refreshFromPersistent();
        assertEquals(2,queueDAO.getTotalDownloads());
    }
    
    @Test
    public void testRemove(){
        DownloadDetailsDTO dd = queueDAO.getNextDownload();
        assertEquals("?q=test1", dd.getDownloadParams());
        //all thedownloads should still be on the queue
        assertEquals(2,queueDAO.getTotalDownloads());
        //now remove
        queueDAO.removeDownloadFromQueue(dd);
        assertEquals(1,queueDAO.getTotalDownloads());
        //now test that the removal has been persisted
        queueDAO.refreshFromPersistent();
        assertEquals(1,queueDAO.getTotalDownloads());
    }

}