/* -*- Mode: Java; tab-width: 2; c-basic-offset: 2; indent-tabs-mode: nil -*- */
/*
 * acp-java : Arcus Java Client Performance benchmark program
 * Copyright 2016 JaM2in Co., Ltd.
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

import net.spy.memcached.collection.CollectionAttributes;
import net.spy.memcached.collection.CollectionOverflowAction;
import net.spy.memcached.collection.CollectionResponse;
import net.spy.memcached.collection.ElementValueType;
import net.spy.memcached.internal.CollectionFuture;

public class torture_map_replace implements client_profile {
  public boolean do_test(client cli) {
    try {
      if (!do_map_test(cli))
        return false;
    } catch (Exception e) {
      System.out.printf("client_profile exception. id=%d exception=%s\n",
          cli.id, e.toString());
      if (cli.conf.print_stack_trace)
        e.printStackTrace();
    }
    return true;
  }

  public boolean do_map_test(client cli) throws Exception {
    // Pick a key
    String key = cli.ks.get_key();

    String[] temp = key.split("-");
    long base = Long.parseLong(temp[1]);
    base = base * 64*1024;

    // Create a map item
    if (!cli.before_request())
      return false;
    ElementValueType vtype = ElementValueType.BYTEARRAY;
    CollectionAttributes attr =
        new CollectionAttributes(cli.conf.client_exptime,
            new Long(4000),
            CollectionOverflowAction.error);
    CollectionFuture<Boolean> fb = cli.next_ac.asyncMopCreate(key, vtype, attr);
    boolean ok = fb.get(cli.conf.client_timeout, TimeUnit.MILLISECONDS);
    if (!ok) {
      System.out.printf("mop create failed. id=%d key=%s: %s\n", cli.id,
          key, fb.getOperationStatus().getResponse());
    }
    if (!cli.after_request(ok))
      return false;
    if (!ok)
      return true;

    // Insert elements
    for (long mkey = base; mkey < base + 4000; mkey++) {
      if (!cli.before_request())
        return false;
      byte[] val = cli.vset.get_value();
      assert(val.length <= 4096);
      String input_mkey = Long.toString(mkey);
      fb = cli.next_ac.asyncMopInsert(key, input_mkey, val,
          null /* Do not auto-create item */);
      ok = fb.get(cli.conf.client_timeout, TimeUnit.MILLISECONDS);
      if (!ok) {
        System.out.printf("mop insert failed. id=%d key=%s mkey=%s: %s\n",
            cli.id, key, input_mkey,
            fb.getOperationStatus().getResponse());
      }
      if (!cli.after_request(ok))
        return false;
      if (!ok)
        return true;
    }

    // Update elements
    for (long mkey = base; mkey < base + 4000; mkey++) {
      if (!cli.before_request())
        return false;
      byte[] val = cli.vset.get_value();
      assert(val.length <= 4096);
      String input_mkey = Long.toString(mkey);
      fb = cli.next_ac.asyncMopUpdate(key, input_mkey, val);
      ok = fb.get(cli.conf.client_timeout, TimeUnit.MILLISECONDS);
      if (!ok) {
        CollectionResponse res = fb.getOperationStatus().getResponse();
        System.out.printf("mop update failed. id=%d key=%s mkey=%s res=%s\n",
            cli.id, key, input_mkey, res.toString());
      }
      if (!cli.after_request(ok))
        return false;
      if (!ok)
        return true;
    }

    if (true)
      return true;

    return true;
  }
}
