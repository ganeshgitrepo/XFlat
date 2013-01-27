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

/**
 * An exception that occurs on an attempt to commit or roll back a transaction that
 * has already been resolved.
 * @author Gordon
 */
public class IllegalTransactionStateException extends TransactionException {
    
    /**
     * Constructs an instance of
     * <code>TransactionException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    IllegalTransactionStateException(String msg) {
        super(msg);
    }
}
