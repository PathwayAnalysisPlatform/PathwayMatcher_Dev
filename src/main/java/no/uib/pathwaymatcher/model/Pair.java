/*
 * Copyright 2017 Luis Francisco Hernández Sánchez.
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
 */
package no.uib.pathwaymatcher.model;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Pair<L,R> {
    private L left;
    private R right;
    public Pair(L left, R right){
        this.left = left;
        this.right = right;
    }
    public L getLeft(){ return left; }
    public R getRight(){ return right; }
    public void setLeft(L l){ this.left = l; }
    public void setRight(R r){ this.right = r; }
}
