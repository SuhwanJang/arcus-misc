/* -*- Mode: Java; tab-width: 2; c-basic-offset: 2; indent-tabs-mode: nil -*- */
/*
 * acp-java : Arcus Java Client Performance benchmark program
 * Copyright 2013-2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.List;

import net.spy.memcached.collection.CollectionAttributes;
import net.spy.memcached.collection.CollectionOverflowAction;
import net.spy.memcached.collection.ElementValueType;
import net.spy.memcached.internal.CollectionFuture;

public class torture_list_ins_getwithdelete implements client_profile {
  public boolean do_test(client cli) {
    try {
      if (!do_list_test(cli))
        return false;
    } catch (Exception e) {
      System.out.printf("client_profile exception. id=%d exception=%s\n", 
                        cli.id, e.toString());
      if (cli.conf.print_stack_trace)
        e.printStackTrace();
    }
    return true;
  }
  
  // create a list and insert elements and get(withDelete=true).

  public boolean do_list_test(client cli) throws Exception {
    // Pick a key
    String key = cli.ks.get_key();

    String[] temp = key.split("-");
    long base = Long.parseLong(temp[1]);
    base = base * 64*1024;

    // Create a list item
    if (!cli.before_request(false))
      return false;
    ElementValueType vtype = ElementValueType.BYTEARRAY;
    CollectionAttributes attr = 
      new CollectionAttributes(cli.conf.client_exptime,
                               new Long(cli.conf.ins_element_size),
                               CollectionOverflowAction.head_trim);
    CollectionFuture<Boolean> fb = cli.next_ac.asyncLopCreate(key, vtype, attr);
    boolean ok = fb.get(cli.conf.client_timeout, TimeUnit.MILLISECONDS);
    if (!ok) {
      System.out.printf("lop create failed. id=%d key=%s: %s\n", cli.id,
                        key, fb.getOperationStatus().getResponse());
    }
    if (!cli.after_request(ok, false))
      return false;
    if (!ok)
      return true;

    // Insert elements
    for (long lkey = base; lkey < base + cli.conf.ins_element_size; lkey++) {
      if (!cli.before_request(false))
        return false;
      byte[] val = cli.vset.get_value();
      assert(val.length <= 4096);

      long n = lkey;
      int i = 0;
      while (n != 0 && i < val.length) {
        val[i] = (byte)(n % 10);
        n = n / 10;
        i++;
      }
      fb = cli.next_ac.asyncLopInsert(key, -1 /* tail */, val,
                                      null /* Do not auto-create item */);
      ok = fb.get(cli.conf.client_timeout, TimeUnit.MILLISECONDS);
      if (!ok) {
        System.out.printf("lop insert failed. id=%d key=%s lkey=%d: %s\n",
                          cli.id, key, lkey,
                          fb.getOperationStatus().getResponse());
      }
      if (!cli.after_request(ok, false))
        return false;
      if (!ok)
        return true;
    }

    // Delete elements
    int index_from = 1;
    int index_to = cli.conf.act_element_size;

    if (!cli.before_request())
      return false;
    CollectionFuture<List<Object>> f =
      cli.next_ac.asyncLopGet(key, index_from, index_to, true /* withDelete */, 
                              true /* dropIfEmpty */);
    List<Object> val = f.get(cli.conf.client_timeout, TimeUnit.MILLISECONDS);
    if (val == null || val.size() <= 0) {
      System.out.printf("Collection_List: LopGet failed." +
                        " id=%d key=%s val.size=%d\n",
                        cli.id, key,
                        val == null ? -1 : val.size());
    }
    if (!cli.after_request(true))
      return false;

    return true;
  }
}
