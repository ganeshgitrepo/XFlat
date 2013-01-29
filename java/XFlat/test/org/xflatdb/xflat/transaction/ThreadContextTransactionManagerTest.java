/* 
*	Copyright 2013 Gordon Burgett and individual contributors
*
*	Licensed under the Apache License, Version 2.0 (the "License");
*	you may not use this file except in compliance with the License.
*	You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*	Unless required by applicable law or agreed to in writing, software
*	distributed under the License is distributed on an "AS IS" BASIS,
*	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*	See the License for the specific language governing permissions and
*	limitations under the License.
*/
package org.xflatdb.xflat.transaction;

import org.xflatdb.xflat.transaction.ThreadContextTransactionManager;
import java.util.concurrent.atomic.AtomicReference;
import org.xflatdb.xflat.db.EngineTransactionManagerTestBase;
import org.xflatdb.xflat.util.FakeDocumentFileWrapper;
import org.jdom2.Document;
import org.junit.After;

/**
 *
 * @author Gordon
 */
public class ThreadContextTransactionManagerTest extends EngineTransactionManagerTestBase {
    
    private AtomicReference<Document> doc = new AtomicReference<>(null);
    
    @After
    public void tearDown(){
        doc.set(null);
    }
    
    @Override
    public ThreadContextTransactionManager getInstance(){
        return new ThreadContextTransactionManager(new FakeDocumentFileWrapper(doc));
    }
}
