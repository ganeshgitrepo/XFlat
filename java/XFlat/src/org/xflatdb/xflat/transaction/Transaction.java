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

import java.io.Closeable;

/**
 *
 * @author Gordon
 */
public interface Transaction extends AutoCloseable {
    
    /**
     * Commits the transaction immediately.
     * @throws TransactionException if an error occurred during the commit.  The
     * transaction manager will automatically revert the transaction upon a commit 
     * error.
     * @throws IllegalTransactionStateException if the transaction has already been
     * committed, reverted, or is revert only.
     */
    void commit() throws TransactionException;
    
    /**
     * Reverts the transaction immediately.
     */
    void revert();
    
    /**
     * Sets the transaction to be "Revert Only".  The transaction will continue
     * as normal, but will throw an {@link IllegalStateException} when {@link #commit() }
     * is called.
     */
    void setRevertOnly();
    
    /**
     * Gets the ID of this transaction.  A Transaction's ID is linked to the time
     * it was created, so a transaction with a higher ID is guaranteed to have
     * been created later.  Transaction IDs are also valid across 
     * @return 
     */
    long getTransactionId();
    
    /**
     * Gets the commit ID of this transaction.  A transaction has a commit ID if
     * it has been committed.  This commit ID is also linked to the time it was
     * created, and can be compared to other transaction IDs to see if this
     * transaction was committed before, during, or after another transaction.
     * @return The transaction's commit ID, or -1 if uncommitted.
     */
    long getCommitId();
    
    /**
     * Returns true if the transaction has been committed.
     * @return 
     */
    boolean isCommitted();
    
    /**
     * Returns true if the transaction has been reverted.
     * @return 
     */
    boolean isReverted();
    
    /**
     * Gets the options with which this transaction was opened.
     * @Return the TransactionOptions object provided when the transaction
     * was opened.
     */
    TransactionOptions getOptions();
    
    /**
     * Closes the current transaction.  If the transaction has yet to be committed
     * or reverted, the transaction is reverted immediately.
     */
    @Override
    void close();
    
    /**
     * Adds a transaction listener for this transaction, if it does not already exist.
     * @param listener 
     */
    void putTransactionListener(TransactionListener listener);
    
    /**
     * Removes a transaction listener for this transaction.
     * @param listener 
     */
    void removeTransactionListener(TransactionListener listener);
}
