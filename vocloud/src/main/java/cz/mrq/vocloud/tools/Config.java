/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.mrq.vocloud.tools;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 * @author voadmin
 */
@Qualifier
@Retention(RUNTIME)
@Target({METHOD, FIELD, PARAMETER, TYPE})
public @interface Config {
}
