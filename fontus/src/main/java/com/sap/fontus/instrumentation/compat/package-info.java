/**
 * Classes that implement special cases for applications that do things we can not handle automatically.
 * <br/>
 * This includes:
 * <ul>
 *     <li>Libraries calling specific functions from native code (sqlite-jdbc)</li>
 *     <li>Libraries that access String properties via Unsafe (github jamm)</li>
 * </ul>
 * <p>
 *     Everything here is rather fragile to API changes, so avoid using this unless absolutely necessary!
 * </p>
 */
package com.sap.fontus.instrumentation.compat;